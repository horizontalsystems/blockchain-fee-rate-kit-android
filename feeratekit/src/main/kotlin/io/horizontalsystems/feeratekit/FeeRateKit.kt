package io.horizontalsystems.feeratekit

import android.content.Context
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.providers.FeeRateProviderManager
import io.horizontalsystems.feeratekit.storage.InMemoryStorage
import io.reactivex.Single
import io.reactivex.functions.Function4

class FeeRateKit(
    providerConfig: FeeProviderConfig,
    private val context: Context,
    var listener: Listener? = null
) {

    interface Listener {
        fun onRefresh(rate: FeeRate)
    }

    private val providerManager: FeeRateProviderManager

    init {
        providerManager = FeeRateProviderManager(providerConfig, InMemoryStorage())
    }

    fun bitcoin(): Single<FeeRate> {
        return getRate(Coin.BITCOIN)
    }

    fun bitcoinCash(): Single<FeeRate> {
        return getRate(Coin.BITCOIN_CASH)
    }

    fun dash(): Single<FeeRate> {
        return getRate(Coin.DASH)
    }

    fun ethereum(): Single<FeeRate> {
        return getRate(Coin.ETHEREUM)
    }

    fun getRate(coinCode: String): Single<FeeRate> {

        Coin.getCoinByCode(code = coinCode)?.also {
            return getRate(it)
        }

        throw IllegalArgumentException()
    }

    fun onUpdate(rate: FeeRate) {
        listener?.onRefresh(rate)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getStatusData(coin: Coin): Single<Any> {

        return (getRate(coin) as Single<Any>).onErrorResumeNext {
            Single.just(Pair(coin.name, "Error:${it.localizedMessage}"))
        }
    }

    fun statusInfo(): Single<Map<String, Any>>? {

        return Single.zip(
            getStatusData(Coin.BITCOIN),
            getStatusData(Coin.ETHEREUM),
            getStatusData(Coin.BITCOIN_CASH),
            getStatusData(Coin.DASH),
            Function4<Any, Any, Any, Any, Array<Any>> { btcRate, ethRate, bchRate, dashRate ->
                arrayOf(btcRate, ethRate, bchRate, dashRate)
            })
            .map { rates ->

                val statusInfo = LinkedHashMap<String, Any>()

                for (rate in rates) {
                    if (rate::class == FeeRate::class) {
                        (rate as FeeRate).let {
                            statusInfo.put(
                                it.coin.name,
                                mapOf(
                                    Pair("HighPriority", it.highPriority),
                                    Pair("MediumPriority", it.mediumPriority),
                                    Pair("LowPriority", it.lowPriority)
                                )
                            )
                        }
                    } else statusInfo.plusAssign((rate as Pair<String,String>))
                }
                statusInfo
            }
    }

    private fun getRate(coin: Coin): Single<FeeRate> {
        return providerManager.getFeeRateProvider(coin).getFeeRates()
    }
}
