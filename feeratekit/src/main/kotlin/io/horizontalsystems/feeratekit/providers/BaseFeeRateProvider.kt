package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.IStorage
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.Single
import java.util.*
import java.util.concurrent.Executors


class BaseFeeRateProvider(
        private val coin: Coin,
        private val feeRateProvider: IFeeRateProvider?,
        private val storage: IStorage)
    : IFeeRateProvider {

    private var executor = Executors.newSingleThreadExecutor()

    override fun getFeeRates(): Single<FeeRate> {

        return feeRateProvider?.getFeeRates()
                ?.doOnError { error ->
                    provideFromFallback()
                }
                ?.doOnSuccess {
                    rate -> storeToFallback(rate)
                }
                ?: Single.just(coin.defaultRate())
    }

    private fun provideFromFallback(): FeeRate? {

        return storage.getFeeRate(this.coin)?.let {
            if ((Date().time / 1000) - it.date <= coin.fallbackDataExpiration)
                it
            else
                null
        }

    }

    private fun storeToFallback(rate: FeeRate) {
        executor.execute {
            storage.setFeeRate(rate)
        }
    }

}

