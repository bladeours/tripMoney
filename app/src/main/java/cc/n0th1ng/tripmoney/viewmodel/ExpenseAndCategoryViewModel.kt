package cc.n0th1ng.tripmoney.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategory
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.data.repository.CategoryRepository
import cc.n0th1ng.tripmoney.data.repository.ExchangeRateRepository
import cc.n0th1ng.tripmoney.data.repository.ExpenseRepository
import cc.n0th1ng.tripmoney.data.repository.TripRepository
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
open class ExpenseAndCategoryViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val tripRepo: TripRepository
) : ViewModel() {

    fun getExpenses(tripId: Int): Flow<PagingData<ExpenseDto>> =
        expenseRepo.getExpensesPaged(tripId).cachedIn(viewModelScope)

    fun save(expense: Expense) {
        viewModelScope.launch {
            expenseRepo.save(expense)
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch {
            expenseRepo.delete(expense)
        }
    }

    fun getCategories(): Flow<List<Category>> = categoryRepo.getCategories()

    fun save(category: Category) {
        viewModelScope.launch {
            categoryRepo.save(category)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSummaryPerCategory(tripId: Int): Flow<List<SummaryPerCategory>> {
        val tripCurrency = tripRepo.getTrip(tripId)?.currency ?: Currencies.default().name
        return getExpensesWithConvertedAmounts(tripId)
            .map { list ->
                // Compute summary
                val sumOfAll = list.sumOf { it.convertedAmount }
                list.groupBy { it.expenseDto.category }
                    .map { (category, expenses) ->
                        val total = expenses.sumOf { it.convertedAmount }
                        SummaryPerCategory(
                            category = category,
                            amount = total,
                            percent = (total / sumOfAll).toFloat(),
                            currency = Currencies.valueOf(tripCurrency)
                        )
                    }.sortedBy { it.percent }.reversed()
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpensesWithConvertedAmounts(tripId: Int): Flow<List<ExpenseDtoWithConvertedAmount>> {
        return expenseRepo.getExpenses(tripId)
            .map { list ->
                list.map { expenseDto ->
                    val convertedAmount =
                        if (expenseDto.expense.currency != expenseDto.trip.currency) {
                            runBlocking {
                                expenseDto.toExpenseDtoWithConvertedAmount()
                            }
                        } else {
                            expenseDto.expense.amount
                        }
                    ExpenseDtoWithConvertedAmount(expenseDto, convertedAmount)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpensesWithConvertedAmountsPaged(tripId: Int): Flow<PagingData<ExpenseDtoWithConvertedAmount>> {
        return expenseRepo.getExpensesPaged(tripId)
            .map { pagingData ->
                pagingData.map { expenseDto ->
                    val convertedAmount =
                        if (expenseDto.expense.currency != expenseDto.trip.currency) {
                            runBlocking {
                                expenseDto.toExpenseDtoWithConvertedAmount()
                            }
                        } else {
                            expenseDto.expense.amount
                        }
                    ExpenseDtoWithConvertedAmount(expenseDto, convertedAmount)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun clearOldRates() {
        viewModelScope.launch {
            exchangeRateRepository.clearOldRates()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun ExpenseDto.toExpenseDtoWithConvertedAmount(): Double {
        return exchangeRateRepository.getRate(
            Currencies.valueOf(this.expense.currency),
            Currencies.valueOf(this.trip.currency),
            LocalDateTime.parse(this.expense.datetime).toLocalDate()
        ) * this.expense.amount
    }

    data class ExpenseDtoWithConvertedAmount(
        val expenseDto: ExpenseDto,
        val convertedAmount: Double
    )
}