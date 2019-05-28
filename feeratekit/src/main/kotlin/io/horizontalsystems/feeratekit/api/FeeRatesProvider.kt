package io.horizontalsystems.feeratekit.api

import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.reactivex.Maybe
import io.reactivex.functions.Function3
import java.util.*

class FeeRatesProvider(private val infuraKey: String?) {

    fun getFeeRatesFromIpfs(url: String, timeoutInSeconds: Int = 60): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val jsonObject =
                    ApiManager().getJson(
                        "https://$url",
                        "ipns/QmXTJZBMMRmBbPun6HFt3tmb3tfYF2usLPxFoacL7G5uMX/blockchain/estimatefee/index.json",
                        timeoutInSeconds
                    )

                val ratesObject = jsonObject.get("rates").asObject()
                val rates = mutableListOf<FeeRate>()

                for (coin in Coin.values()) {
                    val rateForCoin = ratesObject.get(coin.code) ?: continue
                    val rateObject = rateForCoin.asObject()
                    rates.add(
                        FeeRate(
                            coin,
                            rateObject["low_priority"].asLong(),
                            rateObject["medium_priority"].asLong(),
                            rateObject["high_priority"].asLong(),
                            jsonObject["time"].asLong()
                        )
                    )
                }

                subscriber.onSuccess(rates)
                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    fun getGasPriceFromInfura(): Maybe<FeeRate> {
        infuraKey?: return Maybe.empty()

        val infuraUrl = "https://kovan.infura.io/$infuraKey"

        return ApiManager().getGasPrice(infuraUrl)
            .flatMap { mediumBigInt ->
                val medium = mediumBigInt.toLong()
                val low: Long = medium / 3.5.toLong()
                val high: Long = medium * 2

                return@flatMap Maybe.just(
                    FeeRate(
                        coin = Coin.ETHEREUM,
                        date = Date().time,
                        lowPriority = low,
                        mediumPriority = medium,
                        highPriority = high
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
                FeeRate(
                    coin = Coin.BITCOIN,
                    lowPriority = feeInSatoshiPerByte(it.first),
                    mediumPriority = feeInSatoshiPerByte(it.second),
                    highPriority = feeInSatoshiPerByte(it.third),
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
