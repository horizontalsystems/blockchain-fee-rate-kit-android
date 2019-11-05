package io.horizontalsystems.feeratekit.storage

import io.horizontalsystems.feeratekit.model.Coin

class EnumConverter {
    fun fromCoin(coin: Coin): String {
        return coin.code
    }

    fun toCoin(code: String): Coin {
        return Coin.values().first { it.code == code }
    }
}
