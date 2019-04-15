package io.horizontalsystems.feeratekit.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import io.horizontalsystems.feeratekit.FeeRate

@Database(
    version = 1,
    exportSchema = false,
    entities = [FeeRate::class]
)

abstract class KitDatabase : RoomDatabase() {
    abstract val feeRate: FeeRateDao
}
