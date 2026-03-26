package cc.n0th1ng.tripmoney.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategoryRaw
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Upsert
    suspend fun insert(expense: Expense)

    @Query(
        """
        SELECT * FROM expense WHERE trip_id = :tripId
        ORDER BY expense.datetime DESC
    """
    )
    fun expenseDtoPaged(tripId: Int): PagingSource<Int, ExpenseDto>

    @Transaction
    @Query(
    """
        SELECT * FROM expense WHERE trip_id = :tripId
        ORDER BY expense.datetime DESC
    """
    )
    fun expenseDto(tripId: Int): Flow<List<ExpenseDto>>

    @Delete
    suspend fun delete(expense: Expense)
}
