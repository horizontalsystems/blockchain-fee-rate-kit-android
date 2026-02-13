package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.utils.HttpUtils
import kotlin.math.ceil

class BitcoinFeeProvider(private val mempoolSpaceUrl: String, private val torEnabled: Boolean) {

    companion object {
        private const val BLOCKSTREAM_FEE_URL = "https://blockstream.info/api/fee-estimates"
    }

    suspend fun getFeeRate(): RecommendedFees {
        return if (torEnabled) {
            val response = HttpUtils.get(BLOCKSTREAM_FEE_URL, torEnabled)
            val responseObject = response.asObject()
            RecommendedFees(
                fastestFee = ceilFee(responseObject.get("1").asDouble()),
                halfHourFee = ceilFee(responseObject.get("3").asDouble()),
                hourFee = ceilFee(responseObject.get("6").asDouble()),
                economyFee = ceilFee(responseObject.get("7").asDouble()),
                minimumFee = ceilFee(responseObject.get("8").asDouble()),
            )
        } else {
            val response = HttpUtils.get("$mempoolSpaceUrl/api/v1/fees/recommended", torEnabled)
            val responseObject = response.asObject()
            RecommendedFees(
                fastestFee = responseObject.get("fastestFee").asInt(),
                halfHourFee = responseObject.get("halfHourFee").asInt(),
                hourFee = responseObject.get("hourFee").asInt(),
                economyFee = responseObject.get("economyFee").asInt(),
                minimumFee = responseObject.get("minimumFee").asInt(),
            )
        }
    }

    private fun ceilFee(value: Double): Int {
        return maxOf(ceil(value).toInt(), 1)
    }

    data class RecommendedFees(
        val fastestFee: Int,
        val halfHourFee: Int,
        val hourFee: Int,
        val economyFee: Int,
        val minimumFee: Int,
    )

}
