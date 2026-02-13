package io.horizontalsystems.feeratekit.demo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.feeratekit.FeeRateKit
import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.math.BigInteger


class MainViewModel : ViewModel() {
    val feeRateData = MutableLiveData<String>()

    private val sampleBlockchains = listOf( "BTC", "LTC", "BCH", "DASH", "ETH", "BSC")

    private val feeRateKit: FeeRateKit = FeeRateKit(
        FeeProviderConfig(
            ethEvmUrl = FeeProviderConfig.infuraUrl("2a1306f1d12f4c109a4d4fb9be46b02e"),
            ethEvmAuth = "fc479a9290b64a84a15fa6544a130218",
            bscEvmUrl = FeeProviderConfig.defaultBscEvmUrl(),
            mempoolSpaceUrl = "https://mempool.space",
            blockCypherUrl = "https://api.blockcypher.com",
            torEnabled = false,
        )
    )

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = coroutineScope {
                    sampleBlockchains.map { blockchain ->
                        async { getRate(blockchain) }
                    }.map { it.await() }
                }

                val allFees = results.mapIndexed { index, fee ->
                    "${sampleBlockchains[index]} fee: $fee \n"
                }.joinToString(" ")
                feeRateData.postValue(allFees)
            } catch (e: Exception) {
                Log.e("MainViewModel", "fee error: ", e)
            }
        }
    }

    private suspend fun getRate(blockchain: String): BigInteger {
        return when (blockchain) {
            "BTC" -> feeRateKit.bitcoin().economyFee.toBigInteger()
            "LTC" -> feeRateKit.litecoin()
            "BCH" -> feeRateKit.bitcoinCash()
            "DASH" -> feeRateKit.dash()
            "ETH" -> feeRateKit.ethereum()
            "BSC" -> feeRateKit.binanceSmartChain()
            else -> BigInteger.ZERO
        }
    }
}
