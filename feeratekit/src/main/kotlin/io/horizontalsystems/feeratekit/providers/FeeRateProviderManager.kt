package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.IStorage
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeProviderConfig

class FeeRateProviderManager(
    private val config: FeeProviderConfig,
    private val storage: IStorage
) {

    private var providers: MutableMap<Coin, IFeeRateProvider> = mutableMapOf()

    fun getFeeRateProvider(coin: Coin): IFeeRateProvider {
        return providers[coin] ?: addProvider(coin)
    }

    private fun addProvider(coin: Coin): IFeeRateProvider {

        val feeRateProvider: IFeeRateProvider = when (coin) {
            Coin.BITCOIN -> BaseFeeRateProvider(coin, HorsysProvider(coin), storage)
            Coin.LITECOIN -> BaseFeeRateProvider(coin, HorsysProvider(coin), storage)
            Coin.ETHEREUM -> BaseFeeRateProvider(coin, InfuraProvider(config), storage)
            else -> BaseFeeRateProvider(coin, null, storage)
        }

        providers[coin] = feeRateProvider

        return feeRateProvider
    }

}
