package io.horizontalsystems.feeratekit.providers

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.reactivex.Maybe

class FeeRatesProvider(providerConfig: FeeProviderConfig) {

    private val btcCoreProvider = BtcCoreProvider(providerConfig.btcCoreRpcUrl,
                                                  providerConfig.btcCoreRpcUSer,
                                                  providerConfig.btcCoreRpcPassword)
    private val infuraProvider = InfuraProvider(providerConfig.infuraProjectId,
                                                providerConfig.infuraProjectSecret)

    fun getFeeRates(coin: Coin): Maybe<FeeRate> {

        if (coin == Coin.BITCOIN)
            return btcCoreProvider.getFeeRates()
        else if (coin == Coin.ETHEREUM)
            return infuraProvider.getFeeRates()
        else
            return Maybe.empty()
    }

}