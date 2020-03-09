package io.horizontalsystems.feeratekit.model

import java.util.*


data class FeeRate(
        val coin: Coin,
        val lowPriority: Long,
        val lowPriorityDuration: Long,
        val mediumPriority: Long,
        val mediumPriorityDuration: Long,
        val highPriority: Long,
        val highPriorityDuration: Long,
        val date: Long) {

    fun safeLow(): Long = Math.max(coin.minRate(), Math.min(lowPriority, coin.maxRate()))
    fun safeMedium(): Long = Math.max(coin.minRate(), Math.min(mediumPriority, coin.maxRate()))
    fun safeHigh(): Long = Math.max(coin.minRate(), Math.min(highPriority, coin.maxRate()))

}

enum class Coin(
        val code: String,
        val cacheDataExpiration: Int) {

    BITCOIN("BTC", 5 * 60),
    LITECOIN("LTC", 5 * 60),
    BITCOIN_CASH("BCH", 0),
    DASH("DASH", 0),
    ETHEREUM("ETH", 3 * 60);

    companion object {
        fun getCoinByCode(code: String): Coin? {
            return values().find { coin -> coin.code == code }
        }
    }

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
                    date = Date().time / 1000
            )
            LITECOIN -> FeeRate(
                coin = this,
                lowPriority = 1,
                lowPriorityDuration = 30 * 60,
                mediumPriority = 2,
                mediumPriorityDuration = 15 * 60,
                highPriority = 4,
                highPriorityDuration = 3 * 60,
                date = Date().time / 1000
            )
            BITCOIN_CASH -> FeeRate(
                    coin = this,
                    lowPriority = 1,
                    lowPriorityDuration = 240 * 60,
                    mediumPriority = 3,
                    mediumPriorityDuration = 120 * 60,
                    highPriority = 5,
                    highPriorityDuration = 30 * 60,
                    date = Date().time / 1000
            )
            DASH -> FeeRate(
                    coin = this,
                    lowPriority = 1,
                    lowPriorityDuration = 1,
                    mediumPriority = 1,
                    mediumPriorityDuration = 1,
                    highPriority = 2,
                    highPriorityDuration = 1,
                    date = Date().time / 1000
            )
            ETHEREUM -> FeeRate(
                    coin = this,
                    lowPriority = 13_000_000_000,
                    lowPriorityDuration = 30 * 60,
                    mediumPriority = 16_000_000_000,
                    mediumPriorityDuration = 5 * 60,
                    highPriority = 19_000_000_000,
                    highPriorityDuration = 2 * 60,
                    date = Date().time / 1000
            )
        }
    }

    fun maxRate(): Long {
        return when (this) {
            BITCOIN -> 5_000
            LITECOIN -> 5_000
            BITCOIN_CASH -> 500
            DASH -> 500
            ETHEREUM -> 3_000_000_000_000
        }
    }

    fun minRate(): Long {
        return when (this) {
            BITCOIN -> 1
            LITECOIN -> 1
            BITCOIN_CASH -> 1
            DASH -> 1
            ETHEREUM -> 100_000_000
        }
    }
}

class FeeProviderConfig(
        val infuraProjectId: String? = null,
        val infuraProjectSecret: String? = null,
        val infuraApiUrl: String? = null,
        val btcCoreRpcUrl: String? = null,
        val btcCoreRpcUSer: String? = null,
        val btcCoreRpcPassword: String? = null)
