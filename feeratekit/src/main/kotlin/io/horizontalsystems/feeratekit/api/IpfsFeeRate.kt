package io.horizontalsystems.feeratekit.api

import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.reactivex.Maybe

class IpfsFeeRate {

    fun getFeeRate(url: String, timeoutInSeconds: Int = 60): Maybe<List<FeeRate>> {
        return Maybe.create { subscriber ->
            try {
                val apiManager = ApiManager("https://$url")

                val jsonObject =
                    apiManager.getJson("ipns/QmXTJZBMMRmBbPun6HFt3tmb3tfYF2usLPxFoacL7G5uMX/blockchain/estimatefee/index.json", timeoutInSeconds)

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

    companion object {
        const val mainUrl = "ipfs-ext.horizontalsystems.xyz"
        const val fallbackUrl = "ipfs.io"
    }
}
