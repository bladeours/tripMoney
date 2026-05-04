package cc.n0th1ng.tripmoney.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Upsert
    suspend fun insert(expense: Expense): Long


    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
    SELECT * FROM expense
    JOIN category ON expense.category_id = category.id
    WHERE trip_id = :tripId
      AND (
            :search IS NULL
            OR category.name LIKE '%' || :search || '%'
            OR expense.note LIKE '%' || :search || '%'
          )
      AND (
            :categoriesEmpty = 1
            OR expense.category_id IN (:categoryIds)
          )
      AND (
            :startAmount IS NULL OR expense.amount >= :startAmount
          )
      AND (
            :endAmount IS NULL OR expense.amount <= :endAmount
          )

    ORDER BY expense.datetime DESC
        """
    )
    fun expenseDtoPaged(
        tripId: Int,
        search: String?,
        categoryIds: List<Int>,
        categoriesEmpty: Boolean,
        startAmount: Double?,
        endAmount: Double?
    ): PagingSource<Int, ExpenseDto>

    @Transaction
    @Query(
        """
    SELECT * FROM expense
    JOIN category ON expense.category_id = category.id
    WHERE trip_id = :tripId
      AND (
            :search IS NULL
            OR category.name LIKE '%' || :search || '%'
            OR expense.note LIKE '%' || :search || '%'
          )
      AND (
            :categoriesEmpty = 1
            OR expense.category_id IN (:categoryIds)
          )
      AND (
            :startAmount IS NULL OR expense.amount >= :startAmount
          )
      AND (
            :endAmount IS NULL OR expense.amount <= :endAmount
          )

    ORDER BY expense.datetime DESC
"""
    )
    fun expenseDto(
        tripId: Int,
        search: String?,
        categoryIds: List<Int>,
        categoriesEmpty: Boolean,
        startAmount: Double?,
        endAmount: Double?
    ): Flow<List<ExpenseDto>>

    @Query(
        """
    SELECT
        CASE
            WHEN trip.budget = 0 THEN NULL
            ELSE trip.budget - IFNULL(SUM(expense.amount * expense.rate), 0)
        END
    FROM trip
    LEFT JOIN expense ON expense.trip_id = trip.id
    WHERE trip.id = :tripId
    """
    )
    fun budgetLeft(tripId: Int): Flow<Double?>

    @Delete
    suspend fun delete(expense: Expense)
}
