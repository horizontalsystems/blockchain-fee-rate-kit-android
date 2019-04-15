package io.horizontalsystems.feeratekit.storage

import android.arch.persistence.room.TypeConverter
import io.horizontalsystems.feeratekit.Coin

class EnumConverter {
    @TypeConverter
    fun fromCoin(coin: Coin): String {
        return coin.code
    }

    @TypeConverter
    fun toCoin(code: String): Coin {
        return Coin.valueOf(code)
    }
}
