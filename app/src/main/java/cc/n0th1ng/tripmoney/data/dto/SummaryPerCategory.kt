package cc.n0th1ng.tripmoney.data.dto

import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons

data class SummaryPerCategory(
    val category: Category,
    val amount: Double,
    val percent: Float,
    val currency: Currencies
)

data class SummaryPerCategoryRaw(
    val categoryId: Int,
    val categoryName: String,
    val icon: Icons,
    val color: String,
    val amount: Double,
    val currency: String
)

