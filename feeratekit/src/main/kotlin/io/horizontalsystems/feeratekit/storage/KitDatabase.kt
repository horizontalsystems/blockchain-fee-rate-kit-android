package io.horizontalsystems.feeratekit.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import io.horizontalsystems.feeratekit.model.FeeRate

@Database(
    version = 2,
    exportSchema = false,
    entities = [FeeRate::class]
)

abstract class KitDatabase : RoomDatabase() {
    abstract val feeRate: FeeRateDao
}
