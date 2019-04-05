package io.horizontalsystems.feeratekit

interface IStorage {
    fun getFeeRate(coin: Coin): FeeRate?
    fun setFeeRates(rates: List<FeeRate>)
}
