package io.horizontalsystems.feeratekit.storage

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.IStorage

class Storage(private val store: KitDatabase) : IStorage {
    override fun getFeeRate(coin: Coin): FeeRate? {
        return store.feeRate.getByCoin(coin.code)
    }

    override fun setFeeRate(rate: FeeRate) {
        store.feeRate.insert(rate)
    }
}
