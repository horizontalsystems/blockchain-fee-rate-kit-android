package io.horizontalsystems.feeratekit.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel(), FeeRateKit.Listener {
    val feeRate = MutableLiveData<FeeRate>()

    var compositeDisposable = CompositeDisposable()

    val feeRateKit: FeeRateKit = FeeRateKit(
        FeeProviderConfig(
            "2a1306f1d12f4c109a4d4fb9be46b02e",
            "fc479a9290b64a84a15fa6544a130218",
            "",
            "https://btc.horizontalsystems.xyz/apg/"
        ),
        App.instance,
        this
    )

    fun refresh() {

        compositeDisposable.add(feeRateKit.getRate("BTC")
            .onErrorReturn {
                    t ->
                FeeRate(Coin.BITCOIN,0,0,0,0,0,0,0)
            }
            .subscribeOn(Schedulers.io())
            .subscribe() {
                    t -> feeRate.postValue(t)
            })

        compositeDisposable.add(feeRateKit.getRate("ETH")
            .subscribeOn(Schedulers.io())
            .subscribe() {
                    t -> feeRate.postValue(t)
            })

        compositeDisposable.add(feeRateKit.getRate("BCH")
            .subscribeOn(Schedulers.io())
            .subscribe() {
                    t -> feeRate.postValue(t)
            })
    }

    override fun onRefresh(newRate: FeeRate) {
        //this.rates.postValue(newRate)
    }


}
