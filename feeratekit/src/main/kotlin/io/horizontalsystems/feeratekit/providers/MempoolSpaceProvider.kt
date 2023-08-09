package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.utils.HttpUtils
import io.reactivex.Single

class MempoolSpaceProvider(private val mempoolSpaceUrl: String) {
    fun getFeeRate(): Single<RecommendedFees> {
        return Single.create { subscriber ->
            try {
                val response = HttpUtils.get("$mempoolSpaceUrl/api/v1/fees/recommended")
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

    data class RecommendedFees(
        val fastestFee: Int,
        val halfHourFee: Int,
        val hourFee: Int,
        val economyFee: Int,
        val minimumFee: Int,
    )

}
