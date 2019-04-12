package io.horizontalsystems.feeratekit.api

import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.reactivex.Maybe

class IpfsFeeRate {
    private val apiManager = ApiManager("https://ipfs.io")

    fun getFeeRate(): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val jsonObject =
                    apiManager.getJson("ipns/QmXTJZBMMRmBbPun6HFt3tmb3tfYF2usLPxFoacL7G5uMX/blockchain/estimatefee/index.json")

                val ratesObject = jsonObject.get("rates").asObject()
                val rates = mutableListOf<FeeRate>()

                Coin.values().forEach { coin ->
                    val rateObject = ratesObject.get(coin.code).asObject()
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
}
