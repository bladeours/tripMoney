package cc.n0th1ng.tripmoney.screens.statistics

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import cc.n0th1ng.tripmoney.data.dto.SummaryPerCategory
import cc.n0th1ng.tripmoney.data.dto.SummaryPerDay
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import com.composables.icons.materialsymbols.outlined.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(navController: NavController) {
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val summaryPerCategoryList by expenseAndCategoryViewModel.getSummaryPerCategory(currentTripId)
        .collectAsState(emptyList())
    val summaryPerDayList by expenseAndCategoryViewModel.getSummaryPerDay(currentTripId)
        .collectAsState(emptyList())
    val summaryAmount by expenseAndCategoryViewModel.getSummaryAmount(currentTripId)
        .collectAsState(0.0)
    val moneyLeft by expenseAndCategoryViewModel.getBudgetLeft(currentTripId).collectAsState(null)
    StatisticsScreen(
        summaryPerCategoryList,
        summaryPerDayList,
        summaryAmount,
        Currencies.valueOf(currentTrip?.currency ?: Currencies.default().name),
        moneyLeft,
        onDayClicked = {
            date -> navController.navigate(Screens.LIST_EXPENSE + "?dateToScroll=$date")
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    summaryPerCategoryList: List<SummaryPerCategory>,
    summaryPerDayList: List<SummaryPerDay>,
    summaryAmount: Double,
    tripCurrency: Currencies,
    moneyLeft: Double?,
    onDayClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Summary(
                Modifier.weight(1f),
                if (summaryAmount == 0.0) 0.0 else -1 * summaryAmount,
                tripCurrency.name,
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
        SummaryPerCategoryCard(
            modifier = Modifier.heightIn(max = 300.dp),
            summaryPerCategoryList = summaryPerCategoryList
        )
        SummaryPerDayCard(modifier = Modifier.height(300.dp), summaryPerDayList = summaryPerDayList, onDayClicked = onDayClicked)
    }
}


@Composable
fun Summary(
    modifier: Modifier = Modifier,
    amount: Double?,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                if (amount == null) "∞" else
                    "%.2f %s".format(amount, currency),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun SummaryPerCategoryCard(
    summaryPerCategoryList: List<SummaryPerCategory>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (summaryPerCategoryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(cc.n0th1ng.tripmoney.R.string.no_expenses_summary),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .verticalScroll(rememberScrollState()),

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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SummaryPerDayCard(modifier: Modifier = Modifier, summaryPerDayList: List<SummaryPerDay>, onDayClicked: (String) -> Unit) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (summaryPerDayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(cc.n0th1ng.tripmoney.R.string.no_expenses_summary),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(15.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                summaryPerDayList.forEach { it ->
                    DayCard(
                        summaryPerDay = it,
                        onDayClicked = {date -> onDayClicked(date)}
                    )
                }
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
                    .height(30.dp)
                    .fillMaxWidth(0.12f + (0.90f - 0.12f) * summaryPerCategory.percent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 5.dp, horizontal = 10.dp)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCard(modifier: Modifier = Modifier, summaryPerDay: SummaryPerDay, onDayClicked: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "%.2f".format(summaryPerDay.amount),
            style = MaterialTheme.typography.labelSmall,
            fontSize = (MaterialTheme.typography.labelSmall.fontSize.value - 2).sp,
        )
        val width = 45.dp
        Box(
            modifier = Modifier
                .width(width)
                .fillMaxHeight(0.2f + (0.98f - 0.2f) * summaryPerDay.percent)
                .clip(RoundedCornerShape(width / 2))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = {onDayClicked(summaryPerDay.day.toString())})
                .padding(top = 5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .size(width - 10.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(width / 2)
                        )
                        .padding(vertical = 3.dp),
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        text = summaryPerDay.day.format(DateTimeFormatter.ofPattern("dd"))
                    )
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        fontSize = (MaterialTheme.typography.labelSmall.fontSize.value - 2).sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        text = summaryPerDay.day.format(DateTimeFormatter.ofPattern("E"))
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
fun PreviewStatisticScreen() {
    TripMoneyTheme {
        Scaffold {
            StatisticsScreen(
                summaryPerCategoryList,
                summaryPerDayList,
                summaryAmount = 125.24,
                Currencies.entries.random(),
                432.14,
                {}
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewStatisticScreenWithNoData() {
    TripMoneyTheme {
        Scaffold {
            StatisticsScreen(
                emptyList(),
                emptyList(),
                summaryAmount = 0.0,
                Currencies.entries.random(),
                null,
                {}
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

@RequiresApi(Build.VERSION_CODES.O)
val summaryPerDayListRaw = listOf(
    SummaryPerDay(LocalDate.now(), 50.0, 0f),
    SummaryPerDay(LocalDate.now().minusDays(1), 500.23, 0f),
    SummaryPerDay(LocalDate.now().minusDays(2), 1560.53, 0f),
    SummaryPerDay(LocalDate.now().minusDays(3), 700.32, 0f),
    SummaryPerDay(LocalDate.now().minusDays(4), 201.3, 0f),
    SummaryPerDay(LocalDate.now().minusDays(5), 2020.64, 0f),
    SummaryPerDay(LocalDate.now().minusDays(6), 510.43, 0f),
    SummaryPerDay(LocalDate.now().minusDays(7), 3050.12, 0f),
    SummaryPerDay(LocalDate.now().minusDays(8), 264.32, 0f),
    SummaryPerDay(LocalDate.now().minusDays(9), 3596.64, 0f)
)

@RequiresApi(Build.VERSION_CODES.O)
val highestAmount = summaryPerDayListRaw.maxOf { it.amount }

@RequiresApi(Build.VERSION_CODES.O)
val summaryPerDayList = summaryPerDayListRaw.map {
    it.copy(percent = ((it.amount / highestAmount)).toFloat())
}.sortedBy { it.day.toEpochDay() }