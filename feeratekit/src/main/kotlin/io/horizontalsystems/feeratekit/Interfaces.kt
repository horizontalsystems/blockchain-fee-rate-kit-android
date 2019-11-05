package io.horizontalsystems.feeratekit

import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate

interface IStorage {
    fun getFeeRate(coin: Coin): FeeRate?
    fun setFeeRate(rate: FeeRate)
}
