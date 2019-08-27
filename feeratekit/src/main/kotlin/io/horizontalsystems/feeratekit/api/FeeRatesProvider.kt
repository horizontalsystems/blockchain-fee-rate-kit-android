package io.horizontalsystems.feeratekit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.reactivex.Maybe
import io.reactivex.functions.Function3
import java.util.*

class FeeRatesProvider(private val infuraProjectId: String?, private val infuraProjectSecret: String?) {

    fun getFeeRatesFromIpfs(url: String, timeoutInSeconds: Int = 60): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val jsonObject =
                        ApiManager().getJson(
                                "https://$url",
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

    private fun feeRate(coin: Coin, date: Long, rateObject: JsonObject): FeeRate {
        val lowPriority = rateObject["low_priority"].asObject()
        val mediumPriority = rateObject["medium_priority"].asObject()
        val highPriority = rateObject["high_priority"].asObject()

        return FeeRate(coin, lowPriority = lowPriority["rate"].asLong(),
                lowPriorityDuration = getDurationInSeconds(lowPriority),
                mediumPriority = mediumPriority["rate"].asLong(),
                mediumPriorityDuration = getDurationInSeconds(mediumPriority),
                highPriority = highPriority["rate"].asLong(),
                highPriorityDuration = getDurationInSeconds(highPriority),
                date = date)
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

    fun getGasPriceFromInfura(): Maybe<FeeRate> {
        if (infuraProjectId == null || infuraProjectSecret == null)
            return Maybe.empty()

        return ApiManager().getGasPriceFromInfura(infuraProjectId, infuraProjectSecret)
                .flatMap { mediumBigInt ->
                    val medium = mediumBigInt.toLong()
                    val low: Long = medium / 2.toLong()
                    val high: Long = medium * 2

                    val coin = Coin.ETHEREUM
                    val defaultRate = coin.defaultRate()

                    return@flatMap Maybe.just(
                            FeeRate(
                                    coin = coin,
                                    date = Date().time,
                                    lowPriority = low,
                                    lowPriorityDuration = defaultRate.lowPriorityDuration,
                                    mediumPriority = medium,
                                    mediumPriorityDuration = defaultRate.mediumPriorityDuration,
                                    highPriority = high,
                                    highPriorityDuration = defaultRate.highPriorityDuration
                            )
                    )
                }
    }

    fun getFeeRatesFromBCoin(): Maybe<FeeRate> {
        val apiManager = ApiManager()
        val host = "https://btc.horizontalsystems.xyz/apg"

        return Maybe.zip(
                apiManager.getFeeByPriority(ApiManager.bcoinLow, host),
                apiManager.getFeeByPriority(ApiManager.bcoinMedium, host),
                apiManager.getFeeByPriority(ApiManager.bcoinHigh, host),
                Function3<Float, Float, Float, Triple<Float, Float, Float>> { t1, t2, t3 -> Triple(t1, t2, t3) })
                .map {
                    val coin = Coin.BITCOIN
                    val defaultRate = coin.defaultRate()
                    FeeRate(
                            coin = coin,
                            lowPriority = feeInSatoshiPerByte(it.first),
                            lowPriorityDuration = defaultRate.lowPriorityDuration,
                            mediumPriority = feeInSatoshiPerByte(it.second),
                            mediumPriorityDuration = defaultRate.mediumPriorityDuration,
                            highPriority = feeInSatoshiPerByte(it.third),
                            highPriorityDuration = defaultRate.highPriorityDuration,
                            date = Date().time
                    )
                }
    }

    private fun feeInSatoshiPerByte(btcPerKbyte: Float): Long {
        return (btcPerKbyte * 100_000_000 / 1024).toLong()
    }

    companion object {
        const val mainUrl = "ipfs-ext.horizontalsystems.xyz"
        const val fallbackUrl = "ipfs.io"
    }
}
