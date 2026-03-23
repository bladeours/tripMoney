package cc.n0th1ng.tripmoney.screens.listexpense

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.screens.addexpense.AddExpenseBottomSheet
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.colors
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel.ExpenseDtoWithConvertedAmount
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListExpenseScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTrip by settingsViewModel.currentTrip.collectAsState()
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val expensesWithConvertedFlow = expenseAndCategoryViewModel
        .getExpensesWithConvertedAmountsPaged(currentTrip)

    ListExpenseScreen(
        expensesWithConvertedFlow = expensesWithConvertedFlow,
        onSaveExpense = { expenseAndCategoryViewModel.save(it) },
        onDeleteExpense = { expenseAndCategoryViewModel.delete(it) })
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListExpenseScreen(
    expensesWithConvertedFlow: Flow<PagingData<ExpenseDtoWithConvertedAmount>>,
    onSaveExpense: (Expense) -> Unit, onDeleteExpense: (Expense) -> Unit
) {
    val expensesWithConverted = expensesWithConvertedFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var expenseDtoToEdit: ExpenseDto? = null
    val sumMap = remember { mutableStateMapOf<LocalDate, Double>() }

    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { showBottomSheet = true },
            icon = { Icon(Icons.Filled.Add, stringResource(string.add_expense)) },
            text = { Text(text = stringResource(string.add_expense)) },
        )
    })
    {
        LaunchedEffect(expensesWithConverted.itemSnapshotList.items) {
            val items = expensesWithConverted.itemSnapshotList.items
            val newSums = items
                .groupBy { LocalDateTime.parse(it.expenseDto.expense.datetime).toLocalDate() }
                .mapValues { (_, expensesForDay) ->
                    expensesForDay.sumOf { it.convertedAmount }
                }
            sumMap.clear()
            sumMap.putAll(newSums)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            items(
                count = expensesWithConverted.itemCount,
                key = { index -> expensesWithConverted[index]?.expenseDto?.expense?.id ?: index }
            ) { index ->
                val expenseDtoWithConverted = expensesWithConverted[index]
                val expenseDto = expenseDtoWithConverted?.expenseDto
                if (expenseDtoWithConverted != null && expenseDto != null) {
                    val previousExpense =
                        expensesWithConverted.itemSnapshotList.items.getOrNull(index - 1)?.expenseDto
                    val showDayDivider =
                        index == 0 || LocalDateTime.parse(previousExpense?.expense?.datetime)
                            .toLocalDate() != LocalDateTime.parse(expenseDto.expense.datetime)
                            .toLocalDate()
                    Spacer(Modifier
                        .height(5.dp)
                        .background(MaterialTheme.colorScheme.onBackground))
                    if (showDayDivider) {
                        CustomDivider(
                            expenseDto,
                            sumMap.getOrDefault(
                                LocalDateTime.parse(expenseDto.expense.datetime).toLocalDate(), 0.00
                            )
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    SwipeToDeleteExpenseCard(
                        expenseDtoWithConverted = expenseDtoWithConverted,
                        onDelete = { expense -> onDeleteExpense(expense) },
                        onClick = { expenseDto ->
                            expenseDtoToEdit = expenseDto
                            showBottomSheet = true
                        })
                }
            }

        }
        if (showBottomSheet) {
            AddExpenseBottomSheet(
                onSave = { expense ->
                    onSaveExpense(expense)
                    showBottomSheet = false
                    expenseDtoToEdit = null
                },
                onDismiss = {
                    expenseDtoToEdit = null
                    showBottomSheet = false
                },
                expenseDtoToEdit = expenseDtoToEdit,
                state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomDivider(expenseDto: ExpenseDto, sum: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            LocalDateTime.parse(expenseDto.expense.datetime).format(
                DateTimeFormatter.ofPattern("dd EEEE")
            ).toString(),
            modifier = Modifier.background(Color.White.copy(alpha = 0f)),
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Absolute.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(2f))
            Text(
                "%.2f %s".format(sum, expenseDto.trip.currency),
                modifier = Modifier.background(Color.White.copy(alpha = 0f)),
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeToDeleteExpenseCard(
    expenseDtoWithConverted: ExpenseDtoWithConvertedAmount,
    onDelete: (Expense) -> Unit,
    onClick: (ExpenseDto) -> Unit
) {
    var dismissed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    if (!dismissed) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart
                ) {
                    showDialog = true
                    false
                } else {
                    false
                }
            }
        )
        if (showDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    showDialog = false
                    dismissed = true
                    onDelete(expenseDtoWithConverted.expenseDto.expense)
                },
                onCancel = { showDialog = false }
            )
        }

        SwipeToDismissBox(
            modifier = Modifier,
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    Modifier
                        .clip(CardDefaults.elevatedShape)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onError)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(string.delete))
                }
            }
        ) {
            ExpenseCard(expenseDtoWithConverted, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { onCancel() }
    ) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(24.dp)
        ) {
            Text(
                stringResource(string.delete_confirmation),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(string.cancel),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .clickable { onCancel() }
                )
                Text(
                    text = stringResource(string.delete),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { onConfirm() }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseCard(
    expenseDtoWithConverted: ExpenseDtoWithConvertedAmount,
    onClick: (ExpenseDto) -> Unit
) {
    val expenseDto = expenseDtoWithConverted.expenseDto
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(70.dp)
            .combinedClickable(
                enabled = true,
                onClick = { onClick(expenseDto) },
                onLongClick = { onClick(expenseDto) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    painter = painterResource(expenseDto.category.icon.resource),
                    contentDescription = "Category",
                    tint = Color(expenseDto.category.color.toColorInt())
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                ) {
                    Column()
                    {
                        Text(
                            text = expenseDto.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            modifier = Modifier.padding(0.dp),
                            text = expenseDto.expense.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = LocalDateTime.parse(expenseDto.expense.datetime).format(
                            DateTimeFormatter.ofPattern("dd MMM HH:mm")
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column {
                Text(
                    text = "- %.2f ${expenseDto.expense.currency}".format(expenseDto.expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer


                )
                if (expenseDto.expense.currency.lowercase() != expenseDto.trip.currency.lowercase()) {
                    Text(
                        text = "≈ %.2f ${expenseDto.trip.currency}".format(expenseDtoWithConverted.convertedAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewListExpenseScreen() {
    TripMoneyTheme() {
        val pagingData = PagingData.from(sampleExpenseDtoWithConvertedAmountList())
        ListExpenseScreen(
            expensesWithConvertedFlow = MutableStateFlow(pagingData),
            onSaveExpense = {},
            onDeleteExpense = {}
        )

    }
}

@AllPreviews
@Composable
fun PreviewDeleteConfirmationDialog() {
    TripMoneyTheme() {
        DeleteConfirmationDialog(
            onConfirm = {},
            onCancel = {})
    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun sampleExpenseDtoWithConvertedAmountList(): List<ExpenseDtoWithConvertedAmount> {
    val sampleCategories = listOf(
        Category(
            name = "Hotel",
            icon = cc.n0th1ng.tripmoney.utils.Icons.HOTEL,
            color = colors.random()
        ),
        Category(
            name = "Jedzenie",
            icon = cc.n0th1ng.tripmoney.utils.Icons.RESTAURANT,
            color = colors.random()
        ),
        Category(
            name = "Transport",
            icon = cc.n0th1ng.tripmoney.utils.Icons.FLIGHT,
            color = colors.random()
        ),
        Category(
            name = "Rozrywka",
            icon = cc.n0th1ng.tripmoney.utils.Icons.ATTRACTION,
            color = colors.random()
        ),
        Category(
            name = "Zakupy",
            icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
            color = colors.random()
        ),
    )

    val trip = Trip(
        id = 1,
        name = "Vacation",
        currency = "USD",
        startDate = "2026-01-01"
    )

    val startLong = LocalDateTime.now().minusDays(10).toEpochMilli()
    val endLong = LocalDateTime.now().toEpochMilli()

    val result: MutableList<ExpenseDtoWithConvertedAmount> = mutableListOf()
    for (i in 0..15) {
        val category = sampleCategories.random()
        val datetime = if (i > 4) {
            LocalDateTime.ofEpochSecond(
                Random.nextLong(startLong, endLong),
                0,
                ZoneOffset.UTC
            ).toString()
        } else LocalDateTime.now().toString()

        val expense = Expense(
            id = i,
            categoryId = category.id,
            tripId = 1,
            amount = Random.nextDouble(0.1, 300.0),
            currency = Currencies.entries.random().name,
            note = if (i % 3 == 0) "Some note" else "",
            datetime = datetime
        )
        val expenseDto = ExpenseDto(
            expense = expense,
            category = category,
            trip = trip
        )
        result.add(
            ExpenseDtoWithConvertedAmount(
                expenseDto,
                convertedAmount = if (Random.nextBoolean()) Random.nextDouble(
                    0.1,
                    300.0
                ) else expense.amount
            )
        )
    }
    return result
}
