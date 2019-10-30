package io.horizontalsystems.feeratekit.storage

import androidx.room.TypeConverter
import io.horizontalsystems.feeratekit.model.Coin

class EnumConverter {
    @TypeConverter
    fun fromCoin(coin: Coin): String {
        return coin.code
    }

    @TypeConverter
    fun toCoin(code: String): Coin {
        return Coin.values().first { it.code == code }
    }
}
