package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.model.FeeProviderConfig
import io.horizontalsystems.feeratekit.providers.DashHybridFeeProvider
import io.horizontalsystems.feeratekit.providers.EvmProvider
import io.horizontalsystems.feeratekit.providers.BitcoinFeeProvider
import java.math.BigInteger

class FeeRateKit(providerConfig: FeeProviderConfig) {

    private val bitcoinFeeProvider = BitcoinFeeProvider(providerConfig.mempoolSpaceUrl, providerConfig.torEnabled)
    private val ethProvider = EvmProvider(providerConfig.ethEvmUrl, providerConfig.torEnabled, providerConfig.ethEvmAuth)
    private val bscProvider = EvmProvider(providerConfig.bscEvmUrl, providerConfig.torEnabled)
    private val dashHybridFeeProvider = DashHybridFeeProvider(providerConfig.blockCypherUrl, providerConfig.torEnabled)

    suspend fun bitcoin(): BitcoinFeeProvider.RecommendedFees {
        return bitcoinFeeProvider.getFeeRate()
    }

    suspend fun litecoin(): BigInteger {
        return BigInteger("3")
    }

    suspend fun bitcoinCash(): BigInteger {
        return BigInteger("3")
    }

    suspend fun dash(): BigInteger {
        return dashHybridFeeProvider.getFeeRate().fastestFee
    }

    suspend fun ethereum(): BigInteger {
        return ethProvider.getFeeRate()
    }

    suspend fun binanceSmartChain(): BigInteger {
        return bscProvider.getFeeRate()
    }
}
