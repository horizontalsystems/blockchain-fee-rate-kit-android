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
                ?.onErrorReturn {
                    provideFromCache()?.let {
                        it
                    }?:provideFromFallback()
                }
                ?.doOnSuccess {
                    rate -> storeToCache(rate)
                }
                ?: Single.just(coin.defaultRate())
    }

    private fun provideFromCache(): FeeRate? {

        return storage.getFeeRate(this.coin)?.let {
            if ((Date().time / 1000) - it.date <= coin.cacheDataExpiration)
                it
            else
                null
        }
    }

    private fun provideFromFallback(): FeeRate? {
        return this.coin.defaultRate()
    }

    private fun storeToCache(rate: FeeRate) {
        executor.execute {
            storage.setFeeRate(rate)
        }
    }
}

