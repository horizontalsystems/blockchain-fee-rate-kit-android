package io.horizontalsystems.feeratekit

import android.content.Context
import androidx.room.Room
import io.horizontalsystems.feeratekit.api.FeeRatesProvider
import io.horizontalsystems.feeratekit.storage.KitDatabase
import io.horizontalsystems.feeratekit.storage.Storage

class FeeRateKit(infuraProjectId: String? = null, infuraProjectSecret: String? = null, private val context: Context, var listener: Listener? = null) : FeeRateSyncer.Listener {

    interface Listener {
        fun onRefresh(rates: List<FeeRate>)
    }

    private val storage: IStorage
    private val feeRateSyncer: FeeRateSyncer

    init {
        val apiFeeRate = FeeRatesProvider(infuraProjectId, infuraProjectSecret)

        storage = Storage(buildDatabase())
        feeRateSyncer = FeeRateSyncer(storage, apiFeeRate, this)
        feeRateSyncer.start()
    }

    fun bitcoin(): FeeRate {
        return getRate(Coin.BITCOIN)
    }

    fun bitcoinCash(): FeeRate {
        return getRate(Coin.BITCOIN_CASH)
    }

    fun dash(): FeeRate {
        return getRate(Coin.DASH)
    }

    fun ethereum(): FeeRate {
        return getRate(Coin.ETHEREUM)
    }

    fun refresh() {
        feeRateSyncer.refresh()
    }

    override fun onUpdate(rates: List<FeeRate>) {
        listener?.onRefresh(rates)
    }

    private fun getRate(coin: Coin): FeeRate {
        val rateFromStorage = storage.getFeeRate(coin) ?: return coin.defaultRate()
        return getSafeRate(rateFromStorage)
    }

    private fun getSafeRate(rate: FeeRate): FeeRate {
        return FeeRate(
            rate.coin,
            getSafeValue(rate.lowPriority, rate.coin),
            getSafeValue(rate.mediumPriority, rate.coin),
            getSafeValue(rate.highPriority, rate.coin),
            rate.date
        )
    }

    private fun getSafeValue(rate: Long, coin: Coin) =
        if (rate < coin.maximumRate()) rate else coin.maximumRate()

    private fun buildDatabase(): KitDatabase {
        return Room
            .databaseBuilder(context, KitDatabase::class.java, "fee-rate-database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .addMigrations()
            .build()
    }
}
