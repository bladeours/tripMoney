package cc.n0th1ng.tripmoney.screens.statistics

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorLong
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategory
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import com.composables.icons.materialsymbols.outlined.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen() {
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val summaryPerCategoryList by expenseAndCategoryViewModel.getSummaryPerCategory(currentTripId)
        .collectAsState(emptyList())
    val summaryAmount by expenseAndCategoryViewModel.getSummaryAmount(currentTripId)
        .collectAsState(0.0)
    StatisticsScreen(
        summaryPerCategoryList,
        summaryAmount,
        Currencies.valueOf(currentTrip?.currency ?: Currencies.default().name),
        expenseAndCategoryViewModel.getBudgetLeft(currentTripId)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    summaryPerCategoryList: List<SummaryPerCategory>,
    summaryAmount: Double,
    tripCurrency: Currencies,
    moneyLeft: Double
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Summary(
                Modifier.weight(1f), -1 * summaryAmount, tripCurrency.name,
                stringResource(cc.n0th1ng.tripmoney.R.string.total_expenses),
                R.drawable.materialsymbols_ic_payment_arrow_down_outlined,
                iconColor = MaterialTheme.colorScheme.error
            )
            Summary(
                Modifier.weight(1f), moneyLeft, tripCurrency.name,
                stringResource(cc.n0th1ng.tripmoney.R.string.money_left),
                R.drawable.materialsymbols_ic_payments_outlined,
                iconColor = colorResource(cc.n0th1ng.tripmoney.R.color.good_green)
            )
        }
        SummaryPerCategoryCard(summaryPerCategoryList)

    }
}


@Composable
fun Summary(
    modifier: Modifier = Modifier,
    amount: Double,
    currency: String,
    text: String,
    icon: Int,
    iconColor: Color
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceDim,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(5.dp),
                    painter = painterResource(icon),
                    tint = iconColor,
                    contentDescription = null,
                )
                Text(
                    text,
                    style = MaterialTheme.typography.titleSmall
                )

            }
            Text(
                "%.2f %s".format(amount, currency),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun SummaryPerCategoryCard(summaryPerCategoryList: List<SummaryPerCategory>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            summaryPerCategoryList.forEach {
                CategoryCard(
                    summaryPerCategory = it, modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoryCard(modifier: Modifier = Modifier, summaryPerCategory: SummaryPerCategory) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painter = painterResource(summaryPerCategory.category.icon.resource),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.typography.bodyLarge.fontSize.value.dp),
                    tint = Color(summaryPerCategory.category.color.toColorInt())
                )
                Text(
                    "%s".format(
                        summaryPerCategory.category.name,
                        (summaryPerCategory.percent * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(summaryPerCategory.category.color.toColorInt())
                )
            }

            Text(
                "%.2f ${summaryPerCategory.currency}".format(summaryPerCategory.amount),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(0.12f + (0.90f - 0.12f) * summaryPerCategory.percent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(11.dp)
                ) {
                    Text(
                        "%d%%".format((summaryPerCategory.percent * 100).toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun Preview() {
    TripMoneyTheme {
        Scaffold {
            StatisticsScreen(
                summaryPerCategoryList,
                summaryAmount = 125.24,
                Currencies.entries.random(),
                432.14
            )
        }
    }
}

val categories = listOf(
    Category(name = "Jedzenie", icon = Icons.RESTAURANT, color = colors.random()),
    Category(name = "Transport", icon = Icons.FLIGHT, color = colors.random()),
    Category(name = "Rozrywka", icon = Icons.ATTRACTION, color = colors.random()),
    Category(name = "Zakupy", icon = Icons.GROCERIES, color = colors.random()),
    Category(name = "Zakupy1", icon = Icons.GROCERIES, color = colors.random()),
    Category(name = "Zakupy2", icon = Icons.GROCERIES, color = colors.random()),
    Category(name = "Zakupy3", icon = Icons.GROCERIES, color = colors.random())
)

val summaryPerCategoryList = listOf(
    SummaryPerCategory(categories[0], 50.0, 1f, Currencies.PLN),
    SummaryPerCategory(categories[1], 120.0, 0.3f, Currencies.PLN),
    SummaryPerCategory(categories[4], 120.0, 0.3f, Currencies.PLN),
    SummaryPerCategory(categories[2], 80.0, 0.2f, Currencies.PLN),
    SummaryPerCategory(categories[3], 50.0, 0.1f, Currencies.PLN),
    SummaryPerCategory(categories[5], 50.0, 0.0001f, Currencies.PLN),
)