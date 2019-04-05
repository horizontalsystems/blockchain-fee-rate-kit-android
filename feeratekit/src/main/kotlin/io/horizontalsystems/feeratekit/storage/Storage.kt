package io.horizontalsystems.feeratekit.storage

import io.horizontalsystems.feeratekit.Coin
import io.horizontalsystems.feeratekit.FeeRate
import io.horizontalsystems.feeratekit.IStorage

class Storage(private val store: KitDatabase) : IStorage {
    override fun getFeeRate(coin: Coin): FeeRate? {
        return store.feeRate.getByCoin(coin.code)
    }

    override fun setFeeRates(rates: List<FeeRate>) {
        store.feeRate.insert(rates)
    }
}
