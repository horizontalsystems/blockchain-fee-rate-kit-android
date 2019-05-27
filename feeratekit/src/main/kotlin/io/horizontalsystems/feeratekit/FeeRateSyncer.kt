package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.api.FeeRatesProvider
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
        fun onUpdate(rates: List<FeeRate>)
    }

    private val disposables = CompositeDisposable()

    fun start() {
        disposables.add(Observable
            .interval(0, 3, TimeUnit.MINUTES)
            .subscribe {
                syncFeeRate()
            })
    }

    fun refresh() {
        stop()
        start()
    }

    fun stop() {
        disposables.dispose()
    }

    private fun syncFeeRate() {
        feeRatesProvider.getFeeRatesFromIpfs(FeeRatesProvider.mainUrl, 20)
            .onErrorResumeNext(feeRatesProvider.getFeeRatesFromIpfs(FeeRatesProvider.fallbackUrl, 60))
            .subscribeOn(Schedulers.io())
            .subscribe({ rates ->
                storage.setFeeRates(rates)
                listener.onUpdate(rates)
            }, {
                //on error try fetching fees from alternative sources
                getGasPriceFromInfura()
                getBitcoinFeeRateFromBCoin()
            })
            .also {
                disposables.add(it)
            }
    }

    private fun getBitcoinFeeRateFromBCoin() {
        feeRatesProvider.getFeeRatesFromBCoin()
            .subscribeOn(Schedulers.io())
            .subscribe({ rate ->
                storage.setFeeRates(listOf(rate))
                listener.onUpdate(listOf(rate))
            }, {

            })
            .also {
                disposables.add(it)
            }
    }

    private fun getGasPriceFromInfura() {
        feeRatesProvider.getGasPriceFromInfura()
            .subscribeOn(Schedulers.io())
            .subscribe({ rate ->
                storage.setFeeRates(listOf(rate))
                listener.onUpdate(listOf(rate))
            }, {

            })
            .also {
                disposables.add(it)
            }
    }

}
