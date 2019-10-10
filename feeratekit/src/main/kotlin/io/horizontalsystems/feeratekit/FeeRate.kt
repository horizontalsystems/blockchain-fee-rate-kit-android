package io.horizontalsystems.feeratekit

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.horizontalsystems.feeratekit.storage.EnumConverter

@Entity
@TypeConverters(EnumConverter::class)

data class FeeRate(
    @PrimaryKey
    val coin: Coin,
    val lowPriority: Long,
    val lowPriorityDuration: Long,
    val mediumPriority: Long,
    val mediumPriorityDuration: Long,
    val highPriority: Long,
    val highPriorityDuration: Long,
    val date: Long
) {
    fun safeLow(): Long = Math.max(coin.minRate(), Math.min(lowPriority, coin.maxRate()))
    fun safeMedium(): Long = Math.max(coin.minRate(), Math.min(mediumPriority, coin.maxRate()))
    fun safeHigh(): Long = Math.max(coin.minRate(), Math.min(highPriority, coin.maxRate()))
}

enum class Coin(val code: String) {
    BITCOIN("BTC"),
    BITCOIN_CASH("BCH"),
    DASH("DASH"),
    GROESTLCOIN("GRS"),
    ETHEREUM("ETH");

    fun defaultRate(): FeeRate {
        return when (this) {
            BITCOIN -> FeeRate(
                coin = this,
                lowPriority = 20,
                lowPriorityDuration = 1440 * 60,
                mediumPriority = 40,
                mediumPriorityDuration = 120 * 60,
                highPriority = 80,
                highPriorityDuration = 30 * 60,
                date = 1543211299
            )
            BITCOIN_CASH -> FeeRate(
                coin = this,
                lowPriority = 1,
                lowPriorityDuration = 240 * 60,
                mediumPriority = 3,
                mediumPriorityDuration = 120 * 60,
                highPriority = 5,
                highPriorityDuration = 30 * 60,
                date = 1543211299
            )
            DASH -> FeeRate(
                coin = this,
                lowPriority = 1,
                lowPriorityDuration = 1,
                mediumPriority = 1,
                mediumPriorityDuration = 1,
                highPriority = 2,
                highPriorityDuration = 1,
                date = 1557224133
            )
            GROESTLCOIN -> FeeRate(
                    coin = this,
                    lowPriority = 20,
                    lowPriorityDuration = 1,
                    mediumPriority = 20,
                    mediumPriorityDuration = 1,
                    highPriority = 40,
                    highPriorityDuration = 1,
                    date = 1570124714
            )
            ETHEREUM -> FeeRate(
                coin = this,
                lowPriority = 13_000_000_000,
                lowPriorityDuration = 30 * 60,
                mediumPriority = 16_000_000_000,
                mediumPriorityDuration = 5 * 60,
                highPriority = 19_000_000_000,
                highPriorityDuration = 2 * 60,
                date = 1543211299
            )
        }
    }

    fun maxRate(): Long {
        return when (this) {
            BITCOIN -> 5_000
            BITCOIN_CASH -> 500
            DASH -> 500
            GROESTLCOIN -> 500
            ETHEREUM -> 3_000_000_000_000
        }
    }

    fun minRate(): Long {
        return when (this) {
            BITCOIN -> 1
            BITCOIN_CASH -> 1
            DASH -> 1
            GROESTLCOIN -> 1
            ETHEREUM -> 100_000_000
        }
    }
}
