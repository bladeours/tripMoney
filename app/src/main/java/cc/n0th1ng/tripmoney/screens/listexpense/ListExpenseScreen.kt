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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cc.n0th1ng.tripmoney.Filter
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
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel.ExpenseListItemUi
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListExpenseScreen(
    filter: Filter,
    search: String,
    initialAutoOpen: Boolean,
    onAutoOpenConsumed: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val expensesFlow =
        expenseAndCategoryViewModel.getExpensesWithHeadersPaged(currentTripId, search, filter)
    val isRecalculatingRate by tripViewModel.isRecalculating.collectAsState()

    ListExpenseScreen(
        expensesFlow = expensesFlow,
        onSaveExpense = { expenseAndCategoryViewModel.save(it, currentTrip!!) },
        onDeleteExpense = { expenseAndCategoryViewModel.delete(it) },
        isRecalculatingRate = isRecalculatingRate,
        initialAutoOpen = initialAutoOpen,
        onAutoOpenConsumed = onAutoOpenConsumed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListExpenseScreen(
    expensesFlow: Flow<PagingData<ExpenseListItemUi>>,
    onSaveExpense: (Expense) -> Unit, onDeleteExpense: (Expense) -> Unit,
    isRecalculatingRate: Boolean,
    initialAutoOpen: Boolean,
    onAutoOpenConsumed: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialAutoOpen) {
        if (initialAutoOpen) {
            showBottomSheet = true
            onAutoOpenConsumed()
        }
    }

    val items = expensesFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    var expenseDtoToEdit by remember { mutableStateOf<ExpenseDto?>(null) }
    var itemToDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { showBottomSheet = true },
            icon = { Icon(Icons.Filled.Add, stringResource(string.add_expense)) },
            text = { Text(text = stringResource(string.add_expense)) },
        )
    })
    {
        Box {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = listState
            ) {
                items(
                    count = items.itemCount,
                    key = items.itemKey { item ->
                        when (item) {
                            is ExpenseListItemUi.Item -> item.expenseDto.expense.id
                            is ExpenseListItemUi.Header -> "header_${item.date}"
                        }
                    }
                ) { index ->

                    when (val item = items[index]) {

                        is ExpenseListItemUi.Header -> {
                            CustomDivider(
                                date = item.date,
                                sum = item.sum,
                                currency = item.currency
                            )
                        }

                        is ExpenseListItemUi.Item -> {
                            SwipeToDeleteExpenseCard(
                                expenseDto = item.expenseDto,
                                onDelete = { expense -> itemToDelete = expense },
                                onClick = { expenseDto ->
                                    expenseDtoToEdit = expenseDto
                                    showBottomSheet = true
                                }
                            )
                        }

                        null -> {}

                    }
                    Spacer(Modifier.height(10.dp))

                }

            }
        }

        if (itemToDelete != null) {
            DeleteConfirmationDialog(
                onConfirm = {
                    onDeleteExpense(itemToDelete!!)
                    itemToDelete = null
                },
                onCancel = {
                    itemToDelete = null
                }
            )
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
                state = sheetState
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomDivider(date: LocalDate, sum: Double, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            date.format(
                DateTimeFormatter.ofPattern("dd EEEE")
            ).toString(),
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .background(Color.White.copy(alpha = 0f)),
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
                "%.2f %s".format(sum, currency),
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(5.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeToDeleteExpenseCard(
    expenseDto: ExpenseDto,
    onDelete: (Expense) -> Unit,
    onClick: (ExpenseDto) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(expenseDto.expense)
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .clip(CardDefaults.elevatedShape)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(string.delete)
                )
            }
        }
    ) {
        ExpenseCard(
            expenseDto = expenseDto,
            onClick = onClick
        )
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
    expenseDto: ExpenseDto,
    onClick: (ExpenseDto) -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                //TODO
//                .background(
//                    Brush.horizontalGradient(
//                        colorStops = arrayOf(
//                            1f to Color(expenseDto.category.color.toColorInt()),
//                            4f to MaterialTheme.colorScheme.surfaceDim
//                        )
//                    )
//                )
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceDim,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(10.dp),
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            modifier = Modifier.padding(0.dp),
                            text = expenseDto.expense.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = expenseDto.expense.datetime.format(
                            DateTimeFormatter.ofPattern("dd MMM HH:mm")
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column {
                Text(
                    text = "- %.2f ${expenseDto.expense.currency}".format(expenseDto.expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface


                )
                if (expenseDto.expense.currency.lowercase() != expenseDto.trip.currency.lowercase()) {
                    Text(
                        text = "≈ %.2f ${expenseDto.trip.currency}".format(expenseDto.expense.convertedAmount()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
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
            expensesFlow = MutableStateFlow(pagingData),
            onSaveExpense = {},
            onDeleteExpense = {},
            isRecalculatingRate = true,
            false,
            {}
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
private fun sampleExpenseDtoWithConvertedAmountList(): List<ExpenseListItemUi> {
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
        startDate = LocalDate.parse("2026-01-01"),
        endDate = LocalDate.parse("2026-01-11"),
    )

    val startLong = LocalDateTime.now().minusDays(10).toEpochMilli()
    val endLong = LocalDateTime.now().toEpochMilli()

    val result: MutableList<ExpenseListItemUi> = mutableListOf()
    result.add(
        ExpenseListItemUi.Header(
            LocalDateTime.ofEpochSecond(
                Random.nextLong(startLong, endLong),
                0,
                ZoneOffset.UTC
            ).toLocalDate(), Random.nextDouble(0.1, 300.0), Currencies.entries.random().name
        )
    )
    for (i in 0..15) {
        val category = sampleCategories.random()
        val datetime = if (i > 4) {
            LocalDateTime.ofEpochSecond(
                Random.nextLong(startLong, endLong),
                0,
                ZoneOffset.UTC
            )
        } else LocalDateTime.now()

        val expense = Expense(
            id = i,
            categoryId = category.id,
            tripId = 1,
            amount = Random.nextDouble(0.1, 300.0),
            currency = Currencies.entries.random().name,
            note = if (i % 3 == 0) "Some note" else "",
            datetime = datetime,
            rate = if (Random.nextBoolean()) Random.nextDouble(
                0.1,
                5.0
            ) else 1.0
        )


        val expenseDto = ExpenseDto(
            expense = expense,
            category = category,
            trip = trip
        )
        result.add(
            ExpenseListItemUi.Item(expenseDto)
        )
        if (i % 5 == 0) {
            result.add(
                ExpenseListItemUi.Header(
                    datetime.toLocalDate(),
                    Random.nextDouble(0.1, 300.0),
                    Currencies.entries.random().name
                )
            )
        }
    }
    return result
}