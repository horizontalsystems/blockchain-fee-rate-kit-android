package io.horizontalsystems.feeratekit.storage

import androidx.room.*
import io.horizontalsystems.feeratekit.model.FeeRate

@Dao
interface FeeRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rate: FeeRate)

    @Delete
    fun delete(rate: FeeRate)

    @Query("SELECT * FROM FeeRate where coin = :code")
    fun getByCoin(code: String): FeeRate?
}
