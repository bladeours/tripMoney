package cc.n0th1ng.tripmoney.data.repository

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cc.n0th1ng.tripmoney.data.dao.ExpenseDao
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(private val expenseDao: ExpenseDao) {

    @WorkerThread
    suspend fun save(expense: Expense) {
        expenseDao.insert(expense)
    }

    @WorkerThread
    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    fun getExpenses(tripId: Int): Flow<PagingData<ExpenseDto>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { expenseDao.expenseDto(tripId) }
        ).flow
    }
}