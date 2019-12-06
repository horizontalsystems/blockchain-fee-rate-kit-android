package io.horizontalsystems.feeratekit.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.CompositeDisposable


class MainViewModel : ViewModel(){
    val feeRateData = MutableLiveData<Any>()
    var compositeDisposable = CompositeDisposable()

    val feeRateKit: FeeRateKit = FeeRateKit(
        FeeProviderConfig(
            "2a1306f1d12f4c109a4d4fb9be46b02e",
            "fc479a9290b64a84a15fa6544a130218",
            "",
            "https://damp-old-pond.quiknode.io/38708434-ee69-4c9a-84d7-cb0f7f45f2cc/YiBzRob3cfnxTRSvByiyFh2bU93pKzxeyyTHpacaaPF0YnCg9u_cxvvoPIC-3wh6eaQAPyZh5Hd-fDjLGFXCIA==/"
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
