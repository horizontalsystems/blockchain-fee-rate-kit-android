package io.horizontalsystems.feeratekit.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.feeratekit.FeeRate
import io.horizontalsystems.feeratekit.FeeRateKit

class MainViewModel : ViewModel(), FeeRateKit.Listener {
    val rates = MutableLiveData<List<FeeRate>>()

    private val feeRateKit: FeeRateKit = FeeRateKit("2a1306f1d12f4c109a4d4fb9be46b02e", App.instance, this)

    fun refresh() {
        feeRateKit.refresh()
    }

    override fun onRefresh(rates: List<FeeRate>) {
        this.rates.postValue(rates)
    }
}
