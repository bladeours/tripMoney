package cc.n0th1ng.tripmoney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.data.repository.CategoryRepository
import cc.n0th1ng.tripmoney.data.repository.ExchangeRateRepository
import cc.n0th1ng.tripmoney.data.repository.ExpenseRepository
import cc.n0th1ng.tripmoney.service.ExchangeService
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseAndCategoryViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {

    fun getExpenses(tripId: Int): Flow<PagingData<ExpenseDto>> =
        expenseRepo.getExpenses(tripId).cachedIn(viewModelScope)

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

    fun convertAmount(amount: Double, base: Currencies, target: Currencies, date: LocalDate): Flow<Double> {
        return flow {
            emit(amount * exchangeRateRepository.getRate(base, target, date))
        }
    }
}