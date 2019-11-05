package io.horizontalsystems.feeratekit

import android.content.Context
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.providers.FeeRateProviderManager
import io.horizontalsystems.feeratekit.storage.InMemoryStorage
import io.reactivex.Single

class FeeRateKit(
        providerConfig: FeeProviderConfig,
        private val context: Context,
        var listener: Listener? = null) {

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

    private fun getRate(coin: Coin): Single<FeeRate> {
        return providerManager.getFeeRateProvider(coin).getFeeRates()
    }
}
