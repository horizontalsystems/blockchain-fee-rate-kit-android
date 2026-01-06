package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import kotlin.math.ceil

data class RecommendedFees(
    val fastestFee: BigInteger,
    val halfHourFee: BigInteger,
    val hourFee: BigInteger,
    val economyFee: BigInteger,
    val minimumFee: BigInteger,
)

class DashStaticFeeProvider {
    fun getFeeRate(): Single<RecommendedFees> {
        return Single.fromCallable {
            RecommendedFees(
                fastestFee = BigInteger.valueOf(10),    // ~10 duffs/byte for fastest confirmation
                halfHourFee = BigInteger.valueOf(5),    // ~5 duffs/byte for 30 min confirmation
                hourFee = BigInteger.valueOf(2),        // ~2 duffs/byte for 1 hour confirmation
                economyFee = BigInteger.valueOf(1),     // ~1 duff/byte for economy (lowest priority)
                minimumFee = BigInteger.valueOf(1),     // Network minimum
            )
        }.subscribeOn(Schedulers.computation())
    }
}

class DashBlockCypherProvider(private val baseUrl: String, private val torEnabled: Boolean, private val apiToken: String = "") {
    fun getFeeRate(): Single<RecommendedFees> {
        return Single.create<RecommendedFees> { subscriber ->
            try {
                val url = if (apiToken.isNotEmpty()) {
                    "$baseUrl/v1/dash/main?token=$apiToken"
                } else {
                    "$baseUrl/v1/dash/main"
                }

                val response = HttpUtils.get(url, torEnabled)
                val responseObject = response.asObject()

                // BlockCypher provides fee estimates in satoshis per KB (1000 bytes)
                // Get fees as Long to avoid precision issues
                val highFeePerKb = responseObject.get("high_fee_per_kb")?.asLong() ?: 10000L
                val mediumFeePerKb = responseObject.get("medium_fee_per_kb")?.asLong() ?: 5000L
                val lowFeePerKb = responseObject.get("low_fee_per_kb")?.asLong() ?: 2000L

                val recommendedFees = RecommendedFees(
                    fastestFee = BigInteger.valueOf(feePerByte(highFeePerKb)),
                    halfHourFee = BigInteger.valueOf(feePerByte(mediumFeePerKb)),
                    hourFee = BigInteger.valueOf(feePerByte(lowFeePerKb)),
                    // Use the lowest API-provided fee for economy instead of hardcoding
                    economyFee = BigInteger.valueOf(feePerByte(lowFeePerKb)),
                    minimumFee = BigInteger.ONE,
                )
                subscriber.onSuccess(recommendedFees)
            } catch (e: Exception) {
                subscriber.onError(DashFeeException("Failed to fetch fees from BlockCypher", e))
            }
        }.subscribeOn(Schedulers.io())
    }

    // Helper function for correct conversion and rounding
    private fun feePerByte(feePerKb: Long): Long {
        if (feePerKb <= 0) return 1L
        // Use 1000.0 to force floating point division, then round up
        return ceil(feePerKb / 1000.0).toLong().coerceAtLeast(1L)
    }
}

class DashHybridFeeProvider(
    blockCypherUrl: String,
    torEnabled: Boolean,
    apiToken: String = "",
    private val enableFallback: Boolean = true,
    private val timeoutMs: Long = 10000 // 10 seconds timeout
) {
    private val blockCypherProvider = DashBlockCypherProvider(blockCypherUrl, torEnabled, apiToken)
    private val staticProvider = DashStaticFeeProvider()

    fun getFeeRate(): Single<RecommendedFees> {
        return if (enableFallback) {
            getFeeRateWithFallback()
        } else {
            blockCypherProvider.getFeeRate()
                .timeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS, Schedulers.io())
        }
    }

    private fun getFeeRateWithFallback(): Single<RecommendedFees> {
        return blockCypherProvider.getFeeRate()
            .timeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS, Schedulers.io())
            .onErrorResumeNext { error2 ->
                staticProvider.getFeeRate()
            }
    }
}

class DashFeeException(message: String, cause: Throwable? = null) : Exception(message, cause)
