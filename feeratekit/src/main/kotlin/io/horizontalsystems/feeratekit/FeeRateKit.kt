package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.providers.BtcCoreProvider
import io.horizontalsystems.feeratekit.providers.EvmProvider
import io.reactivex.Single
import java.math.BigInteger

class FeeRateKit(providerConfig: FeeProviderConfig) {

    private val btcProvider = BtcCoreProvider(providerConfig)
    private val ethProvider = EvmProvider(providerConfig.ethEvmUrl, providerConfig.ethEvmAuth)
    private val bscProvider = EvmProvider(providerConfig.bscEvmUrl)

    fun bitcoin(blockCount: Int): Single<BigInteger> {
        return btcProvider.getFeeRate(blockCount)
    }

    fun litecoin(): Single<BigInteger> {
        return Single.just(BigInteger("1"))
    }

    fun bitcoinCash(): Single<BigInteger> {
        return Single.just(BigInteger("3"))
    }

    fun dash(): Single<BigInteger> {
        return Single.just(BigInteger("1"))
    }

    fun ethereum(): Single<BigInteger> {
        return ethProvider.getFeeRate()
    }

    fun binanceSmartChain(): Single<BigInteger> {
       return bscProvider.getFeeRate()
    }
}
