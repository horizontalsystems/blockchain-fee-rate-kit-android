package io.horizontalsystems.feeratekit.storage

import android.arch.persistence.room.*
import io.horizontalsystems.feeratekit.FeeRate

@Dao
interface FeeRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rates: List<FeeRate>)

    @Delete
    fun delete(rate: FeeRate)

    @Query("SELECT * FROM FeeRate where coin = :code")
    fun getByCoin(code: String): FeeRate?
}
