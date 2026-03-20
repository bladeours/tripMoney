package cc.n0th1ng.tripmoney.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.ExchangeRate
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Upsert
    suspend fun insert(exchangeRate: ExchangeRate)

    @Query("SELECT * FROM exchange_rate WHERE id = :id")
    suspend fun getById(id: String): ExchangeRate?

    @Query("DELETE FROM exchange_rate WHERE DATE(date) < :cutoffDate")
    suspend fun deleteOldRates(cutoffDate: String)
}
