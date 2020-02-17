package io.horizontalsystems.feeratekit.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainViewModel : ViewModel(){
    val feeRateData = MutableLiveData<Any>()
    var compositeDisposable = CompositeDisposable()

    val feeRateKit: FeeRateKit = FeeRateKit(
        FeeProviderConfig(
            "2a1306f1d12f4c109a4d4fb9be46b02e",
            "fc479a9290b64a84a15fa6544a130218",
            "",
            "http://134.209.138.9/fee_low/0ab6af109397934023af7eca5ec19e5f",
            "http://134.209.138.9/fee_avg/e3cc8f8c828dddfd02914a74f0287b6c",
            "http://134.209.138.9/fee_high/4d34b0a386827c81ef966e3af78712a6"
        ),
        App.instance
    )

    fun getStatusInfo() {

        feeRateKit.statusInfo()?.let {
            compositeDisposable.add(
                it.onErrorReturn { t ->
                    mutableMapOf(Pair("No data:", ""))
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe() { t ->
                        feeRateData.postValue(t)
                    })
        }
    }

    fun refresh() {

        compositeDisposable.add(feeRateKit.getRate("BTC")
            .onErrorReturn { t ->
                FeeRate(Coin.BITCOIN, 0, 0, 0, 0, 0, 0, 0)
            }
            .subscribeOn(Schedulers.io())
            .subscribe() { t ->
                feeRateData.postValue(t)
            })

        compositeDisposable.add(feeRateKit.getRate("ETH")
            .subscribeOn(Schedulers.io())
            .subscribe() { t ->
                feeRateData.postValue(t)
            })

        compositeDisposable.add(feeRateKit.getRate("BCH")
            .subscribeOn(Schedulers.io())
            .subscribe() { t ->
                feeRateData.postValue(t)
            })
    }
}
