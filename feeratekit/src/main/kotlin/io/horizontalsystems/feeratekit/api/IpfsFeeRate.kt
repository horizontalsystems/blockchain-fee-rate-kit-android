package io.horizontalsystems.feeratekit.api

import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.reactivex.Maybe

class IpfsFeeRate {
    private val apiManager = ApiManager("https://ipfs-ext.horizontalsystems.xyz") // https://ipfs.io

    fun getFeeRate(): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val jsonObject =
                    apiManager.getJson("ipns/QmXTJZBMMRmBbPun6HFt3tmb3tfYF2usLPxFoacL7G5uMX/blockchain/estimatefee/index.json")

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
}
