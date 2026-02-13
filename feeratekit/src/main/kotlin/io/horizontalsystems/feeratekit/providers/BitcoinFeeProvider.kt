package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single

class BitcoinFeeProvider(private val mempoolSpaceUrl: String, private val torEnabled: Boolean) {

    companion object {
        private const val BLOCKSTREAM_FEE_URL = "https://blockstream.info/api/fee-estimates"
    }

    fun getFeeRate(): Single<RecommendedFees> {
        return if (torEnabled) {
            Single.create { subscriber ->
                try {
                    val response = HttpUtils.get(BLOCKSTREAM_FEE_URL, torEnabled)
                    val responseObject = response.asObject()
                    val recommendedFees = RecommendedFees(
                        fastestFee = ceilFee(responseObject.get("1").asDouble()),
                        halfHourFee = ceilFee(responseObject.get("3").asDouble()),
                        hourFee = ceilFee(responseObject.get("6").asDouble()),
                        economyFee = ceilFee(responseObject.get("144").asDouble()),
                        minimumFee = ceilFee(responseObject.get("1008").asDouble()),
                    )
                    subscriber.onSuccess(recommendedFees)
                } catch (e: Exception) {
                    subscriber.onError(e)
                }
            }
        } else {
            Single.create { subscriber ->
                try {
                    val response = HttpUtils.get("$mempoolSpaceUrl/api/v1/fees/recommended", torEnabled)
                    val responseObject = response.asObject()
                    val recommendedFees = RecommendedFees(
                        fastestFee = responseObject.get("fastestFee").asInt(),
                        halfHourFee = responseObject.get("halfHourFee").asInt(),
                        hourFee = responseObject.get("hourFee").asInt(),
                        economyFee = responseObject.get("economyFee").asInt(),
                        minimumFee = responseObject.get("minimumFee").asInt(),
                    )
                    subscriber.onSuccess(recommendedFees)
                } catch (e: Exception) {
                    subscriber.onError(e)
                }
            }
        }
    }

    private fun ceilFee(value: Double): Int {
        return maxOf(Math.ceil(value).toInt(), 1)
    }

    data class RecommendedFees(
        val fastestFee: Int,
        val halfHourFee: Int,
        val hourFee: Int,
        val economyFee: Int,
        val minimumFee: Int,
    )

}
