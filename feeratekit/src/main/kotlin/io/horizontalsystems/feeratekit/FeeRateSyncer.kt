package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.api.IpfsFeeRate
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FeeRateSyncer(
    private val storage: IStorage,
    private val ipfsFeeRate: IpfsFeeRate,
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
        ipfsFeeRate.getFeeRate(IpfsFeeRate.mainUrl, 20)
            .onErrorResumeNext(ipfsFeeRate.getFeeRate(IpfsFeeRate.fallbackUrl,60))
            .subscribeOn(Schedulers.io())
            .subscribe({ rates ->
                storage.setFeeRates(rates)
                listener.onUpdate(rates)
            }, {
                // error happened
            })
            .also {
                disposables.add(it)
            }
    }

}
