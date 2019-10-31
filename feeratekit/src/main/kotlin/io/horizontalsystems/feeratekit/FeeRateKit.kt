package io.horizontalsystems.feeratekit

import android.content.Context
import androidx.room.Room
import io.horizontalsystems.feeratekit.providers.FeeRatesProvider
import io.horizontalsystems.feeratekit.model.Coin
import io.horizontalsystems.feeratekit.model.FeeRate
import io.horizontalsystems.feeratekit.providers.FeeProviderConfig
import io.horizontalsystems.feeratekit.storage.KitDatabase
import io.horizontalsystems.feeratekit.storage.Storage

class FeeRateKit(
    providerConfig: FeeProviderConfig,
    private val context: Context,
    var listener: Listener? = null
) : FeeRateSyncer.Listener {

    interface Listener {
        fun onRefresh(rate: FeeRate)
    }

    private val storage: IStorage
    private val feeRateSyncer: FeeRateSyncer

    init {
        val feeRateProvider = FeeRatesProvider(providerConfig)

        storage = Storage(buildDatabase())
        feeRateSyncer = FeeRateSyncer(storage, feeRateProvider, this)
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

    fun getRate(coinCode: String): FeeRate? {

        Coin.getCoinByCode(code = coinCode)?.also {
            return getRate(it)
        }

        return null
    }

    fun refresh() {
        feeRateSyncer.refresh()
    }

    override fun onUpdate(rate: FeeRate) {
        listener?.onRefresh(rate)
    }

    private fun getRate(coin: Coin): FeeRate {
        return storage.getFeeRate(coin) ?: coin.defaultRate()
    }

    private fun buildDatabase(): KitDatabase {
        return Room
            .databaseBuilder(context, KitDatabase::class.java, "fee-rate-database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .addMigrations()
            .build()
    }
}
