package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.providers.FeeRatesProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FeeRateSyncer(
    private val storage: IStorage,
    private val feeRatesProvider: FeeRatesProvider,
    private val listener: Listener
) {

    interface Listener {
        fun onUpdate(rate: FeeRate)
    }

    companion object {
        const val BTC_CORE_SYNC_INTERVAL: Int = 6 //minutes
        const val INFURA_SYNC_INTERVAL: Int = 3 //minutes
    }

    private val disposables = CompositeDisposable()

    fun start() {
        disposables.add(Observable
                            .interval(0, BTC_CORE_SYNC_INTERVAL.toLong(), TimeUnit.MINUTES)
                            .subscribe {
                                syncFeeRate(Coin.BITCOIN)
                            })

        disposables.add(Observable
                            .interval(0, INFURA_SYNC_INTERVAL.toLong(), TimeUnit.MINUTES)
                            .subscribe {
                                syncFeeRate(Coin.ETHEREUM)
                            })
    }

    fun refresh() {
        stop()
        start()
    }

    fun stop() {
        disposables.dispose()
    }

    private fun syncFeeRate(coin: Coin) {
        feeRatesProvider.getFeeRates(coin)
            .subscribeOn(Schedulers.io())
            .subscribe({ newRate ->
                           storage.setFeeRate(newRate)
                           listener.onUpdate(newRate)
                       }, {
                           //on error try fetching fees from alternative sources
                           //getGasPriceFromInfura()
                           //getBitcoinFeeRateFromBCoin()
                       })
            .also {
                disposables.add(it)
            }
    }

}
