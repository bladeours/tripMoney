package cc.n0th1ng.tripmoney.data.dto

import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.utils.Currencies
import java.time.LocalDate

data class SummaryPerCategory(
    val category: Category,
    val amount: Double,
    val percent: Float,
    val currency: Currencies
)

data class SummaryPerDay(
    val day: LocalDate,
    val amount: Double,
    val percent: Float
)