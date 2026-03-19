package cc.n0th1ng.tripmoney.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto

@Dao
interface ExpenseDao {
    @Upsert
    suspend fun insert(expense: Expense)

    @Transaction
    @Query(
        """
        SELECT * FROM expense WHERE trip_id = :tripId
        ORDER BY DATETIME(expense.datetime) DESC
    """
    )
    fun expenseDto(tripId: Int): PagingSource<Int, ExpenseDto>

    @Delete
    suspend fun delete(expense: Expense)
}
