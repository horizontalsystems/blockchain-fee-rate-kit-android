package io.horizontalsystems.feeratekit.providers

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.Maybe
import java.net.URL
import java.util.logging.Logger

class IpfsProvider {

    private val logger = Logger.getLogger("IpfsProvider")

    private val mainUrl = "ipfs-ext.horizontalsystems.xyz"
    private val fallbackUrl = "ipfs.io"
    private val timeoutInSeconds: Int = 60

    fun getFeeRates(): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val jsonObject = getJson(
                    "https://$mainUrl",
                    "ipns/QmXTJZBMMRmBbPun6HFt3tmb3tfYF2usLPxFoacL7G5uMX/blockchain/estimatefee/feerates.json",
                    timeoutInSeconds
                )

                val ratesObject = jsonObject.get("feerates").asObject()
                val rates = mutableListOf<FeeRate>()

                for (coin in Coin.values()) {
                    val rateForCoin = ratesObject.get(coin.code) ?: continue
                    val rateObject = rateForCoin.asObject()
                    rates.add(
                        feeRate(coin, jsonObject["time"].asLong(), rateObject)
                    )
                }

                subscriber.onSuccess(rates)
                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }


    @Throws
    fun getJson(host: String, file: String, timeoutInSeconds: Int): JsonObject {
        return getJsonValue(host, file, timeoutInSeconds).asObject()
    }

    private fun getJsonValue(host: String, file: String, timeoutInSeconds: Int): JsonValue {
        val resource = "$host/$file"

        logger.info("Fetching $resource")

        return URL(resource)
            .openConnection()
            .apply {
                connectTimeout = 5000
                readTimeout = timeoutInSeconds * 1000
                setRequestProperty("Accept", "application/json")
            }
            .getInputStream()
            .use {
                Json.parse(it.bufferedReader())
            }
    }


    private fun feeRate(coin: Coin, date: Long, rateObject: JsonObject): FeeRate {
        val lowPriority = rateObject["low_priority"].asObject()
        val mediumPriority = rateObject["medium_priority"].asObject()
        val highPriority = rateObject["high_priority"].asObject()

        return FeeRate(
            coin, lowPriority = lowPriority["rate"].asLong(),
            lowPriorityDuration = getDurationInSeconds(lowPriority),
            mediumPriority = mediumPriority["rate"].asLong(),
            mediumPriorityDuration = getDurationInSeconds(mediumPriority),
            highPriority = highPriority["rate"].asLong(),
            highPriorityDuration = getDurationInSeconds(highPriority),
            date = date
        )
    }

    private fun getDurationInSeconds(priorityObject: JsonObject): Long {
        val duration = priorityObject["duration"].asLong()
        val durationUnit = priorityObject["duration_unit"].asString()

        return duration * when (durationUnit) {
            "SECONDS" -> 1
            "MINUTES" -> 60
            else -> 60 * 60 //"HOURS"
        }
    }
}