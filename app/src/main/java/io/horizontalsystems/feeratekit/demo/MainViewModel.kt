package io.horizontalsystems.feeratekit.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.providers.FeeProviderConfig

class MainViewModel : ViewModel(), FeeRateKit.Listener {
    val rates = MutableLiveData<List<FeeRate>>()

    private var feeRatesList: List<FeeRate> = mutableListOf()

    private val feeRateKit: FeeRateKit = FeeRateKit(
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
        feeRateKit.refresh()
    }

    override fun onRefresh(newRate: FeeRate) {

        updateList(newRate)
        this.rates.postValue(feeRatesList)
    }

    private fun updateList(newRate: FeeRate){

        feeRatesList.find { rate -> rate.coin == newRate.coin }?.let {
            feeRatesList = feeRatesList.minus(it)
        }

        feeRatesList = feeRatesList.plus(newRate)
    }
}
