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
        SELECT expense.*, category.*
        FROM expense
        JOIN category ON expense.category_id = category.id
        WHERE expense.trip_id = :tripId
          AND
           (
            (:filter IS NULL OR category.name LIKE '%' || :filter || '%')
            OR (:filter IS NULL OR expense.note LIKE '%' || :filter || '%')
          )
        ORDER BY expense.datetime DESC
        """
    )
    fun expenseDtoPaged(tripId: Int, filter: String): PagingSource<Int, ExpenseDto>

    @Transaction
    @Query(
    """
        SELECT * FROM expense
        JOIN category ON expense.category_id = category.id
        WHERE trip_id = :tripId
          AND
           (
            (:filter IS NULL OR category.name LIKE '%' || :filter || '%')
            OR (:filter IS NULL OR expense.note LIKE '%' || :filter || '%')
          )
        ORDER BY expense.datetime DESC
    """
    )
    fun expenseDto(tripId: Int, filter: String): Flow<List<ExpenseDto>>

    @Query("""
    SELECT trip.budget - IFNULL(SUM(expense.amount * expense.rate), 0)
    FROM trip
    LEFT JOIN expense ON expense.trip_id = trip.id
    WHERE trip.id = :tripId
    """)
    fun budgetLeft(tripId: Int): Double

    @Delete
    suspend fun delete(expense: Expense)
}
