package cc.n0th1ng.tripmoney.service

import androidx.room.withTransaction
import cc.n0th1ng.tripmoney.data.TripDatabase
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.data.repository.CategoryRepository
import cc.n0th1ng.tripmoney.data.repository.ExchangeRateRepository
import cc.n0th1ng.tripmoney.data.repository.ExpenseRepository
import cc.n0th1ng.tripmoney.data.repository.TripRepository
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors
import kotlinx.coroutines.flow.first
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.time.LocalDateTime
import javax.inject.Inject

class ImportService @Inject() constructor(
    private val tripRepo: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val db: TripDatabase
) {
    suspend fun importCSV(
        csv: String,
        filename: String,
        onError: (Exception) -> Unit,
        onSuccess: () -> Unit
    ) {
        try {
            db.withTransaction {
                val parser = CSVParser.parse(
                    csv, CSVFormat.DEFAULT.builder().setHeader(
                        "date", "category", "currency", "amount", "note"
                    ).setSkipHeaderRecord(true).get()
                )
                val records = parser.records.toList()
                val currency = records.map { it.get("currency") }.groupingBy { it }.eachCount()
                    .maxBy { it.value }.key
                val startDate =
                    records.map { it.get("date") }
                        .minOfOrNull { LocalDateTime.parse(it.substringBefore(",")).toLocalDate() }
                val endDate =
                    records.map { it.get("date") }
                        .maxOfOrNull { LocalDateTime.parse(it.substringBefore(",")).toLocalDate() }
                if (!Currencies.names().contains(currency.uppercase()))
                    throw Exception("There is no such currency as $currency")
                if (startDate == null || endDate == null) throw Exception("There is no start or end date")
                val trip = Trip(
                    name = filename.substringBefore(".csv"),
                    startDate = startDate,
                    endDate = endDate,
                    currency = currency
                )
                val tripId = tripRepo.save(trip)

                records.forEach {
                    val dateTime = LocalDateTime.parse(it.get("date").substringBefore(","))
                    val amount = it.get("amount")
                    val currency = it.get("currency")
                    val categoryName = it.get("category")
                    val note = it.get("note")
                    val category = categoryRepository.getByName(categoryName).first()
                    val categoryId = category?.id
                        ?: categoryRepository.save(
                            Category(
                                name = categoryName,
                                icon = Icons.entries.random(),
                                color = colors.random()
                            )
                        ).toInt()

                    val rate = exchangeRateRepository.getRate(
                        Currencies.valueOf(currency),
                        Currencies.valueOf(trip.currency),
                        dateTime.toLocalDate(),
                    )
                    val expense = Expense(
                        amount = amount.toDouble(),
                        currency = currency,
                        note = note,
                        datetime = dateTime,
                        categoryId = categoryId,
                        tripId = tripId.toInt(),
                    )
                    expenseRepository.save(expense.copy(rate = rate))
                }
            }
            onSuccess()
        } catch (ex: Exception) {
            onError(ex)
        }
    }
}