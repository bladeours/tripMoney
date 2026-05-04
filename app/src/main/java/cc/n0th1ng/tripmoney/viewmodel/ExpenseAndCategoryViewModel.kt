package cc.n0th1ng.tripmoney.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import cc.n0th1ng.tripmoney.Filter
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategory
import cc.n0th1ng.tripmoney.data.dto.SummaryPerDay
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.data.repository.CategoryRepository
import cc.n0th1ng.tripmoney.data.repository.ExchangeRateRepository
import cc.n0th1ng.tripmoney.data.repository.ExpenseRepository
import cc.n0th1ng.tripmoney.data.repository.TripRepository
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
open class ExpenseAndCategoryViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val tripRepo: TripRepository
) : ViewModel() {

    fun getBudgetLeft(tripId: Int): Flow<Double?> {
        return expenseRepo.getBudgetLeft(tripId)
    }

    fun getExpensesDtoPaged(
        tripId: Int,
        search: String = "",
        filter: Filter = Filter()
    ): Flow<PagingData<ExpenseDto>> =
        expenseRepo.getExpensesDtoPaged(tripId, search, filter).cachedIn(viewModelScope)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpensesWithHeadersPaged(
        tripId: Int,
        search: String = "",
        filter: Filter
    ): Flow<PagingData<ExpenseListItemUi>> {
        val pagingFlow = getExpensesDtoPaged(tripId, search, filter)
        val sumsFlow = getDailySums(tripId, search, filter)
        val tripFlow = tripRepo.getTrip(tripId)
        return combine(pagingFlow, sumsFlow, tripFlow) { pagingData, sums, trip ->
            val currency = trip?.currency ?: ""
            pagingData
                .map<ExpenseDto, ExpenseListItemUi> {
                    ExpenseListItemUi.Item(it)
                }
                .insertSeparators { before, after ->
                    if (after == null) return@insertSeparators null
                    val afterItem = after as ExpenseListItemUi.Item
                    val afterDate = afterItem.expenseDto.expense.datetime.toLocalDate()
                    val beforeDate = (before as? ExpenseListItemUi.Item)
                        ?.expenseDto
                        ?.expense
                        ?.datetime
                        ?.toLocalDate()

                    if (before == null || beforeDate != afterDate) {
                        ExpenseListItemUi.Header(
                            date = afterDate,
                            sum = sums[afterDate] ?: 0.0,
                            currency = currency
                        )
                    } else {
                        null
                    }
                }

        }.cachedIn(viewModelScope)
    }

    fun getExpensesDto(
        tripId: Int,
        search: String = "",
        filter: Filter = Filter()
    ): Flow<List<ExpenseDto>> =
        expenseRepo.getExpensesDto(tripId, search, filter)

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(expense: Expense, trip: Trip, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val rate = exchangeRateRepository.getRate(
                Currencies.valueOf(expense.currency),
                Currencies.valueOf(trip.currency),
                expense.datetime.toLocalDate()
            )
            val id = expenseRepo.save(expense.copy(rate = rate))
            onComplete(id.toInt())
        }
    }


    fun delete(expense: Expense) {
        viewModelScope.launch {
            expenseRepo.delete(expense)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            categoryRepo.delete(category)
        }
    }

    fun getCategories(): Flow<List<Category>> = categoryRepo.getCategories()
    fun getArchivedCategories(): Flow<List<Category>> = categoryRepo.getArchivedCategories()

    fun save(category: Category) {
        viewModelScope.launch {
            categoryRepo.save(category)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generateCSVToFile(tripId: Int, file: File) {
        file.writer().use { writer ->
            CSVPrinter(
                writer,
                CSVFormat.DEFAULT.withHeader("date", "category", "currency", "amount")
            ).use { printer ->
                expenseRepo.getExpensesDto(tripId).first().forEach { expenseDto ->
                    printer.printRecord(
                        expenseDto.expense.datetime,
                        expenseDto.category.name,
                        expenseDto.expense.currency,
                        expenseDto.expense.amount
                    )

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailySums(tripId: Int, search: String, filter: Filter): Flow<Map<LocalDate, Double>> {
        return getExpensesDto(tripId, search, filter)
            .map { expenses ->
                expenses.groupBy { it.expense.datetime.toLocalDate() }
                    .mapValues { (_, list) ->
                        list.sumOf { it.expense.amount * it.expense.rate }
                    }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSummaryAmount(tripId: Int): Flow<Double> {
        return getExpensesDto(tripId).map { list ->
            list.sumOf { it.expense.amount * it.expense.rate }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSummaryPerCategory(tripId: Int): Flow<List<SummaryPerCategory>> {
        val tripFlow = tripRepo.getTrip(tripId)
        val expensesFlow = getExpensesDto(tripId)

        return tripFlow.combine(expensesFlow) { trip, expenses ->
            val tripCurrency = trip?.currency ?: Currencies.default().name
            val sumOfAll = expenses.sumOf { it.expense.convertedAmount() }

            expenses.groupBy { it.category }
                .map { (category, expensesForCategory) ->
                    val total = expensesForCategory.sumOf { it.expense.convertedAmount() }
                    SummaryPerCategory(
                        category = category,
                        amount = total,
                        percent = (total / sumOfAll).toFloat(),
                        currency = Currencies.valueOf(tripCurrency)
                    )
                }
                .sortedByDescending { it.percent }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSummaryPerDay(tripId: Int): Flow<List<SummaryPerDay>> {
        val tripFlow = tripRepo.getTrip(tripId)
        val expensesFlow = getExpensesDto(tripId)

        return tripFlow.combine(expensesFlow) { trip, expenses ->
            val summaryPerDayRaw = expenses.groupBy { it.expense.datetime.toLocalDate() }
                .map { (day, expensesForDay) ->
                    val total = expensesForDay.sumOf { it.expense.convertedAmount() }
                    SummaryPerDay(
                        amount = total,
                        day = day,
                        percent = 0.0f
                    )
                }
                .sortedByDescending { it.day }


            val highestAmount =
                if (summaryPerDayRaw.isEmpty()) 1.0 else summaryPerDayRaw.maxOf { it.amount }
            summaryPerDayRaw.map {
                it.copy(percent = ((it.amount / highestAmount)).toFloat())
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
    sealed class ExpenseListItemUi {
        data class Item(val expenseDto: ExpenseDto) : ExpenseListItemUi()
        data class Header(val date: LocalDate, val sum: Double, val currency: String) :
            ExpenseListItemUi()
    }
}

