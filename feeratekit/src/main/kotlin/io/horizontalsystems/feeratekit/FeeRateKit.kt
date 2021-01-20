package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.providers.BtcCoreProvider
import io.horizontalsystems.feeratekit.providers.InfuraProvider
import io.reactivex.Single
import java.math.BigInteger

class FeeRateKit(providerConfig: FeeProviderConfig) {

    private val btcProvider = BtcCoreProvider(providerConfig)
    private val infuraProvider = InfuraProvider(providerConfig)

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
        return Single.just(BigInteger("4"))
    }

    fun ethereum(): Single<BigInteger> {
        return infuraProvider.getFeeRate()
    }

}
