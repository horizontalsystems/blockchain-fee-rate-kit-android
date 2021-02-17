package io.horizontalsystems.feeratekit.demo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import io.reactivex.functions.Function;


class MainViewModel : ViewModel() {
    val feeRateData = MutableLiveData<String>()
    private val compositeDisposable = CompositeDisposable()

    private val sampleBlockchains = listOf( "BTC", "LTC", "BCH", "DASH", "ETH", "BSC")

    private val feeRateKit: FeeRateKit = FeeRateKit(
        FeeProviderConfig(
            btcCoreRpcUrl = "https://btc.horizontalsystems.xyz/rpc",
            ethEvmUrl = FeeProviderConfig.infuraUrl("2a1306f1d12f4c109a4d4fb9be46b02e"),
            ethEvmAuth = "fc479a9290b64a84a15fa6544a130218",
            bscEvmUrl = FeeProviderConfig.defaultBscEvmUrl()
        )
    )

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun refresh() {
        val requests = sampleBlockchains.map { getRate(it) }
        val listMerger = Function<Array<Any>, List<BigInteger>> { args ->
            args.map { it as BigInteger }
        }

        compositeDisposable.add(
            Single.zip(requests, listMerger)
                .subscribeOn(Schedulers.io())
                .subscribe({ list ->
                    val allFees = list.mapIndexed { index, fee ->
                        "${sampleBlockchains[index]} fee: $fee \n"
                    }.joinToString(" ")
                    feeRateData.postValue(allFees)
                }, {
                    Log.e("MainViewModel", "fee error: ", it)
                })
        )

    }

    private fun getRate(blockchain: String): Single<BigInteger> {
        return when (blockchain) {
            "BTC" -> feeRateKit.bitcoin(8)
            "LTC" -> feeRateKit.litecoin()
            "BCH" -> feeRateKit.bitcoinCash()
            "DASH" -> feeRateKit.dash()
            "ETH" -> feeRateKit.ethereum()
            "BSC" -> feeRateKit.binanceSmartChain()
            else -> Single.just(BigInteger.ZERO)
        }
    }
}
