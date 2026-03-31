package cc.n0th1ng.tripmoney.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cc.n0th1ng.tripmoney.data.dao.ExpenseDao
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategoryRaw
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.utils.Currencies
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val exchangeRateRepository: ExchangeRateRepository
) {

    @WorkerThread
    suspend fun save(expense: Expense) {
        expenseDao.insert(expense)
    }

    @WorkerThread
    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    fun getExpensesDtoPaged(tripId: Int, filter: String): Flow<PagingData<ExpenseDto>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { expenseDao.expenseDtoPaged(tripId, filter) }
        ).flow
    }

    fun getExpensesDto(tripId: Int, filter: String = ""): Flow<List<ExpenseDto>> {
        return expenseDao.expenseDto(tripId, filter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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