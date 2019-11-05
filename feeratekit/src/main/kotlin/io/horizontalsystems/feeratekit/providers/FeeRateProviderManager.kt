package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.IStorage
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig

class FeeRateProviderManager(
        private val config: FeeProviderConfig,
        private val storage: IStorage) {

    private var providers: MutableMap<Coin, IFeeRateProvider> = mutableMapOf()

    fun getFeeRateProvider(coin: Coin): IFeeRateProvider {
        return providers.get(coin) ?: addProvider(coin)
    }

    private fun addProvider(coin: Coin): IFeeRateProvider {

        val feeRateProvider: IFeeRateProvider

        if (coin == Coin.BITCOIN)
            feeRateProvider = BaseFeeRateProvider(coin, BtcCoreProvider(config), storage)
        else if (coin == Coin.ETHEREUM)
            feeRateProvider = BaseFeeRateProvider(coin, InfuraProvider(config), storage)
        else
            feeRateProvider = BaseFeeRateProvider(coin, null, storage)

        providers.set(coin, feeRateProvider)

        return feeRateProvider
    }

}
