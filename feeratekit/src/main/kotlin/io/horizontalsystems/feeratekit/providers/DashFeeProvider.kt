package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.utils.HttpUtils
import kotlinx.coroutines.withTimeout
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
    suspend fun getFeeRate(): RecommendedFees {
        return RecommendedFees(
            fastestFee = BigInteger.valueOf(10),
            halfHourFee = BigInteger.valueOf(5),
            hourFee = BigInteger.valueOf(2),
            economyFee = BigInteger.valueOf(1),
            minimumFee = BigInteger.valueOf(1),
        )
    }
}

class DashBlockCypherProvider(private val baseUrl: String, private val torEnabled: Boolean, private val apiToken: String = "") {
    suspend fun getFeeRate(): RecommendedFees {
        try {
            val url = if (apiToken.isNotEmpty()) {
                "$baseUrl/v1/dash/main?token=$apiToken"
            } else {
                "$baseUrl/v1/dash/main"
            }

            val response = HttpUtils.get(url, torEnabled)
            val responseObject = response.asObject()

            val highFeePerKb = responseObject.get("high_fee_per_kb")?.asLong() ?: 10000L
            val mediumFeePerKb = responseObject.get("medium_fee_per_kb")?.asLong() ?: 5000L
            val lowFeePerKb = responseObject.get("low_fee_per_kb")?.asLong() ?: 2000L

            return RecommendedFees(
                fastestFee = BigInteger.valueOf(feePerByte(highFeePerKb)),
                halfHourFee = BigInteger.valueOf(feePerByte(mediumFeePerKb)),
                hourFee = BigInteger.valueOf(feePerByte(lowFeePerKb)),
                economyFee = BigInteger.valueOf(feePerByte(lowFeePerKb)),
                minimumFee = BigInteger.ONE,
            )
        } catch (e: Exception) {
            throw DashFeeException("Failed to fetch fees from BlockCypher", e)
        }
    }

    private fun feePerByte(feePerKb: Long): Long {
        if (feePerKb <= 0) return 1L
        return ceil(feePerKb / 1000.0).toLong().coerceAtLeast(1L)
    }
}

class DashHybridFeeProvider(
    blockCypherUrl: String,
    torEnabled: Boolean,
    apiToken: String = "",
    private val enableFallback: Boolean = true,
    private val timeoutMs: Long = 10000
) {
    private val blockCypherProvider = DashBlockCypherProvider(blockCypherUrl, torEnabled, apiToken)
    private val staticProvider = DashStaticFeeProvider()

    suspend fun getFeeRate(): RecommendedFees {
        return if (enableFallback) {
            getFeeRateWithFallback()
        } else {
            withTimeout(timeoutMs) {
                blockCypherProvider.getFeeRate()
            }
        }
    }

    private suspend fun getFeeRateWithFallback(): RecommendedFees {
        return try {
            withTimeout(timeoutMs) {
                blockCypherProvider.getFeeRate()
            }
        } catch (e: Exception) {
            staticProvider.getFeeRate()
        }
    }
}

class DashFeeException(message: String, cause: Throwable? = null) : Exception(message, cause)
