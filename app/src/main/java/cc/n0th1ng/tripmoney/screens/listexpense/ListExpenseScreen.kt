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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.screens.addexpense.AddExpenseBottomSheet
import cc.n0th1ng.tripmoney.service.ExchangeService
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListExpenseScreen() {
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val currentTrip by settingsViewModel.currentTrip.collectAsState()
    val expenses = expenseAndCategoryViewModel.getExpenses(currentTrip).collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var expenseDtoToEdit: ExpenseDto? = null

    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { showBottomSheet = true },
            icon = { Icon(Icons.Filled.Add, stringResource(string.add_expense)) },
            text = { Text(text = stringResource(string.add_expense)) },
        )
    })
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            items(
                count = expenses.itemCount,
                key = { index -> expenses[index]?.expense?.id ?: index }
            ) { index ->
                val expenseDto = expenses[index]
                if (expenseDto != null) {
                    val previousExpense = expenses.itemSnapshotList.items.getOrNull(index - 1)
                    val showDayDivider =
                        index == 0 || LocalDateTime.parse(previousExpense?.expense?.datetime)
                            .toLocalDate() != LocalDateTime.parse(expenseDto.expense.datetime)
                            .toLocalDate()
                    Spacer(Modifier.height(5.dp))
                    if (showDayDivider) {
                        CustomDivider(expenseDto)
                    }
                    Spacer(Modifier.height(5.dp))
                    SwipeToDeleteExpenseCard(
                        expenseDto = expenseDto,
                        onDelete = { expense -> expenseAndCategoryViewModel.delete(expense) },
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
                    expenseAndCategoryViewModel.save(expense)
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
fun CustomDivider(expenseDto: ExpenseDto) {
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
            modifier = Modifier.background(Color.White.copy(alpha = 0f))
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeToDeleteExpenseCard(
    expenseDto: ExpenseDto,
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
                    onDelete(expenseDto.expense)
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
            ExpenseCard(expenseDto, onClick = onClick)
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
                    MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(24.dp)
        ) {
            Text(
                stringResource(string.delete_confirmation),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(string.cancel),
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .clickable { onCancel() }
                )
                Text(
                    text = stringResource(string.delete),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onConfirm() }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseCard(expenseDto: ExpenseDto, onClick: (ExpenseDto) -> Unit) {
    ElevatedCard(
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
                            fontWeight = FontWeight.Bold,
                            lineHeight = 5.sp
                        )
                        Text(
                            modifier = Modifier.padding(0.dp),
                            text = expenseDto.expense.note,
                            fontSize = 11.sp,
                            lineHeight = 5.sp
                        )
                    }

                    Text(
                        text = LocalDateTime.parse(expenseDto.expense.datetime).format(
                            DateTimeFormatter.ofPattern("dd MMM HH:mm")
                        ),
                        fontSize = 12.sp,
                    )
                }
            }
            Column {
                Text(
                    text = "- %.2f ${expenseDto.expense.currency}".format(expenseDto.expense.amount),
                    fontWeight = FontWeight.Bold
                )
                if (expenseDto.expense.currency.lowercase() != expenseDto.trip.currency.lowercase()) {
                    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
                    val amount by
                    expenseAndCategoryViewModel.convertAmount(
                        amount = expenseDto.expense.amount,
                        base = Currencies.valueOf(expenseDto.expense.currency),
                        target = Currencies.valueOf(expenseDto.trip.currency),
                        date = LocalDateTime.parse(expenseDto.expense.datetime).toLocalDate()
                    ).collectAsState(initial = 0.0)
                    Text(
                        text = "≈ %.2f ${expenseDto.trip.currency}".format(amount),
                        fontSize = 12.sp
                    )
                }

            }
        }
    }
}