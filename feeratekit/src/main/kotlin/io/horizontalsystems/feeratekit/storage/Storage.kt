package io.horizontalsystems.feeratekit.storage

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.IStorage

class InMemoryStorage() : IStorage {

    private var ratesDb: MutableMap<Coin, FeeRate> = mutableMapOf()

    override fun getFeeRate(coin: Coin): FeeRate? {
        return ratesDb.get(coin)
    }

    override fun setFeeRate(rate: FeeRate) {
        ratesDb.set(rate.coin, rate)
    }

}
