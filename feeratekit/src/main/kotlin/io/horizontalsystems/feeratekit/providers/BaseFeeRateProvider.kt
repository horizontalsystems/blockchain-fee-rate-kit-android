package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.IStorage
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*


class BaseFeeRateProvider(
        private val coin: Coin,
        private val feeRateProvider: IFeeRateProvider?,
        private val storage: IStorage)
    : IFeeRateProvider {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun getFeeRates(): Single<FeeRate> {

        return feeRateProvider?.getFeeRates()
                ?.doOnError { error ->
                    provideFromFallback() ?:error
                }
                ?.doOnSuccess {
                    rate -> storeToFallback(rate)
                }
                ?: Single.just(coin.defaultRate())
    }

    private fun provideFromFallback(): FeeRate? {

        storage.getFeeRate(this.coin)?.let {
            if ((Date().time / 1000) - it.date <= coin.fallbackDataExpiration)
                return it
        }

        return null
    }

    private fun storeToFallback(rate: FeeRate) {
        coroutineScope.async(Dispatchers.IO) {
            storage.setFeeRate(rate)
        }
    }

}

