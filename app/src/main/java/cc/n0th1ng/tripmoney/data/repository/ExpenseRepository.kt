package cc.n0th1ng.tripmoney.data.repository

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cc.n0th1ng.tripmoney.Filter
import cc.n0th1ng.tripmoney.data.dao.ExpenseDao
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.screens.listexpense.toEpochMilli
import cc.n0th1ng.tripmoney.utils.Currencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val exchangeRateRepository: ExchangeRateRepository
) {

    fun getBudgetLeft(tripId: Int): Flow<Double?> {
        return expenseDao.budgetLeft(tripId)
    }

    @WorkerThread
    suspend fun save(expense: Expense): Long {
        return expenseDao.insert(expense)
    }

    @WorkerThread
    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    fun getExpensesDtoPaged(
        tripId: Int,
        search: String,
        filter: Filter,
    ): Flow<PagingData<ExpenseDto>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            pagingSourceFactory = {
                val categoryIds = filter.categories.map { it.id }
                expenseDao.expenseDtoPaged(
                    tripId = tripId,
                    search = search.takeIf { it.isNotBlank() },
                    categoryIds = categoryIds,
                    categoriesEmpty = categoryIds.isEmpty(),
                    startAmount = filter.startAmount,
                    endAmount = filter.endAmount,
                    startDate = if(filter.startDate == LocalDate.MIN) null else filter.startDate.toEpochMilli(),
                    endDate = if(filter.endDate == LocalDate.MAX) null else  filter.endDate.plusDays(1).toEpochMilli(),
                )
            }
        ).flow
    }

    fun getExpensesDto(
        tripId: Int,
        search: String = "",
        filter: Filter = Filter()
    ): Flow<List<ExpenseDto>> {
        val categoryIds = filter.categories.map { it.id }
        return expenseDao.expenseDto(
            tripId = tripId,
            search = search.takeIf { it.isNotBlank() },
            categoryIds = categoryIds,
            categoriesEmpty = categoryIds.isEmpty(),
            startAmount = filter.startAmount,
            endAmount = filter.endAmount,
            startDate = if(filter.startDate == LocalDate.MIN) null else filter.startDate.toEpochMilli(),
            endDate = if(filter.endDate == LocalDate.MAX) null else  filter.endDate.plusDays(1).toEpochMilli(),
        )
    }

    suspend fun recalculateTripExpenses(tripId: Int) {
        val expenses = getExpensesDto(tripId).first()
        expenses.forEach { expenseDto ->
            val newRate = exchangeRateRepository.getRate(
                Currencies.valueOf(expenseDto.expense.currency),
                Currencies.valueOf(expenseDto.trip.currency),
                expenseDto.expense.datetime.toLocalDate()
            )
            save(
                expenseDto.expense.copy(rate = newRate)
            )
        }
    }

}