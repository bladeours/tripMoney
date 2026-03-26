package cc.n0th1ng.tripmoney.screens.addexpense

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.screens.listexpense.CategorySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.DateTimePicker
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.colors
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import com.composables.icons.materialsymbols.outlined.R.drawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddExpenseBottomSheet(
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit,
    expenseDtoToEdit: ExpenseDto?,
    state: SheetState
) {
    val tripViewModel: TripViewModel = hiltViewModel()
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)

    val categories by expenseAndCategoryViewModel.getCategories().collectAsState(emptyList())
    AddExpenseBottomSheet(
        onSave = onSave,
        onDismiss = onDismiss,
        expenseDtoToEdit = expenseDtoToEdit,
        state = state,
        currentTrip = currentTrip!!,
        categories = categories
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddExpenseBottomSheet(
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit,
    expenseDtoToEdit: ExpenseDto?,
    state: SheetState,
    currentTrip: Trip,
    categories: List<Category>
) {
    val currentTripId = currentTrip.id

    if (categories.isEmpty()) {
        return
    }

    var amount by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.amount?.toString() ?: "0.00"
        )
    }
    var equationResult by remember { mutableDoubleStateOf(0.0) }
    val dummyFocusRequester = remember { FocusRequester() }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var currency by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.currency ?: currentTrip.currency
        )
    }
    var category by remember { mutableStateOf(expenseDtoToEdit?.category ?: categories[0]) }
    var datetime by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.datetime ?: LocalDateTime.now()
        )
    }
    var note by remember { mutableStateOf(expenseDtoToEdit?.expense?.note ?: "") }
    var enableSave by remember { mutableStateOf(expenseDtoToEdit != null) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp),
                text = stringResource(if (expenseDtoToEdit == null) R.string.add_expense else R.string.edit_expense),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Start
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = amount.ifEmpty { "0.00" },
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (amount.contains(Regex("[+\\/*-]\\d+"))) "%.2f".format(
                                equationResult
                            ) else "",
                            fontSize = 14.sp,
                        )
                    }
                    CurrencyButton(onClick = { showCurrencyDialog = true }, text = currency)
                }
                Box(
                    modifier = Modifier
                        .size(0.dp)
                        .focusRequester(dummyFocusRequester)
                        .focusable()
                )
                NoteInput(
                    note = note,
                    onTextChange = { newNote -> note = newNote },
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = dummyFocusRequester
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showDateTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,

                        ) {
                        Text(
                            text = datetime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                            fontSize = 17.sp
                        )
                    }
                    CategoryButton(
                        onClick = { showCategoryDialog = true },
                        category = category,
                        modifier = Modifier.weight(1f)
                    )

                }

                NumberKeyboard(
                    modifier = Modifier.fillMaxWidth(),
                    onOperatorClick = { operator ->
                        if (amount.isDoubleTwoDigitsOrEquation() && amount.contains(Regex("[+\\/*-]\\d+"))) {
                            amount = evaluate(amount).toString()
                        }
                        val newText = amount + operator
                        if (newText.isDoubleTwoDigitsOrEquation()) {
                            amount = newText
                            enableSave = false
                        }
                    },
                    onNumberClick = { number ->
                        val newText = (if (amount == "0.00") "" else amount) + number
                        if (newText.isDoubleTwoDigitsOrEquation()) {
                            amount = newText
                            equationResult = evaluate(amount)
                            enableSave = equationResult > 0
                        } else if (amount == "0.00") {
                            enableSave = false
                        }
                        dummyFocusRequester.requestFocus()
                    },
                    onBackspaceClick = {
                        if (amount == "0.00") return@NumberKeyboard
                        amount = amount.safeSubstring(0, amount.length - 1)
                        enableSave = amount.isDoubleTwoDigitsOrEquation()
                        equationResult = evaluate(amount)
                        enableSave = amount.isDoubleTwoDigitsOrEquation() && equationResult > 0
                    },
                )

                SaveButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enableSave,
                    onClick = {
                        val expenseToSave = Expense(
                            amount = equationResult,
                            currency = currency,
                            note = note,
                            datetime = datetime,
                            categoryId = category.id,
                            tripId = currentTripId
                        )
                        onSave(
                            if (expenseDtoToEdit == null) expenseToSave
                            else expenseToSave.copy(id = expenseDtoToEdit.expense.id)
                        )
                    })
            }
        }
    }


    if (showDateTimePicker) {
        DateTimePicker(datetime, onChange = { newDateTime ->
            datetime = newDateTime
            showDateTimePicker = false
        })
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { selectedCurrency ->
                showCurrencyDialog = false
                currency = selectedCurrency
            },
            selected = currency
        )
    }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { selectedCategory ->
                showCategoryDialog = false
                category = selectedCategory
            },
            selected = category,
            categories = categories
        )
    }
}

fun String.safeSubstring(start: Int, end: Int): String {
    return try {
        this.substring(start, end)
    } catch (_: Exception) {
        "0.00"
    }
}

private fun evaluate(equation: String): Double {
    if (equation.isEmpty()) return 0.0

    val operatorIndex = equation.indexOfFirstIndexed { i, c ->
        i != 0 && c in "+-*/"
    }

    if (operatorIndex == -1) return equation.toDouble()

    val leftString = equation.substring(0, operatorIndex)
    val rightString = equation.substring(operatorIndex + 1)

    if (leftString.isEmpty() || rightString.isEmpty()) return 0.0

    val left = leftString.toDouble()
    val right = rightString.toDouble()

    return when (equation[operatorIndex]) {
        '+' -> left + right
        '-' -> left - right
        '*' -> left * right
        '/' -> left / right
        else -> 0.0
    }
}

private inline fun String.indexOfFirstIndexed(predicate: (index: Int, Char) -> Boolean): Int {
    for (i in indices) {
        if (predicate(i, this[i])) return i
    }
    return -1
}

private fun String.isDoubleTwoDigitsOrEquation(): Boolean {
    return this != "0.00" && this.matches(Regex("^(-?(0\\.?|0\\.\\d{1,2}|[1-9]\\d*(\\.\\d{0,2})?))([+\\/*-](0\\.?|0\\.\\d{1,2}|[1-9]\\d*(\\.\\d{0,2})?)?)?$"))
}

@Composable
fun NoteInput(
    note: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester
) {
    var text by remember { mutableStateOf(note) }

    OutlinedTextField(
        modifier = modifier,
        label = { Text(stringResource(R.string.note)) }, value = note, onValueChange = { newText ->
            text = newText
            onTextChange(text)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusRequester.requestFocus()
            }
        )
    )
}

@Composable
fun CurrencyButton(modifier: Modifier = Modifier, onClick: () -> Unit, text: String) {
    Button(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Text(text)
    }
}

@Composable
fun CategoryButton(onClick: () -> Unit, category: Category, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors()
            .copy(containerColor = Color(category.color.toColorInt()), contentColor = Color.Black)
    ) {
        Icon(
            modifier = Modifier.padding(end = 10.dp),
            painter = painterResource(category.icon.resource),
            contentDescription = stringResource(R.string.category),
        )
        Text(category.name)
    }
}

@Composable
fun SaveButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.save)
        )
    }
}

@Composable
fun NumberKeyboard(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onOperatorClick: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keyboard.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "backspace" -> KeyboardButton(
                            icon = painterResource(drawable.materialsymbols_ic_arrow_left_alt_outlined),
                            onClick = onBackspaceClick,
                            modifier = Modifier
                                .weight(1f),
                            containerColor = MaterialTheme.colorScheme.primary
                        )

                        "+", "/", "-", "*" -> KeyboardButton(
                            text = key,
                            onClick = { onOperatorClick(key) },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        else -> KeyboardButton(
                            text = key,
                            onClick = { onNumberClick(key) },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: Painter? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(2.5f),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        when {
            text != null -> Text(
                text,
                style = MaterialTheme.typography.titleMedium
            )

            icon != null -> Icon(painter = icon, contentDescription = null)
        }
    }
}

val keyboard = listOf(
    listOf("+", "-", "*", "/"),
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "backspace")
)


@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@AllPreviews
@Composable
fun PreviewAddExpenseDisabled() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    CoroutineScope(Dispatchers.IO).launch {
        sheetState.show()
    }

    TripMoneyTheme {
        AddExpenseBottomSheet(
            onSave = {},
            onDismiss = {},
            expenseDtoToEdit = null,
            state = sheetState,
            currentTrip = Trip(
                1,
                "Trip",
                LocalDate.parse("2020-01-01"),
                Currencies.entries.random().name
            ),
            categories = categoriesToPreview
        )
    }

}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@AllPreviews
@Composable
fun PreviewAddExpenseEnabled() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    CoroutineScope(Dispatchers.IO).launch {
        sheetState.show()
    }

    TripMoneyTheme {
        AddExpenseBottomSheet(
            onSave = {},
            onDismiss = {},
            expenseDtoToEdit = ExpenseDto(
                Expense(
                    amount = 10.31,
                    currency = "PLN",
                    note = "some note",
                    datetime = LocalDateTime.now(),
                    categoryId = 1,
                    tripId = 1
                ),
                category = categoriesToPreview[0],
                Trip(1, "Włochy", LocalDate.parse("2025-01-02"), "PLN")
            ),
            state = sheetState,
            currentTrip = Trip(
                1,
                "Trip",
                LocalDate.parse("2020-01-01"),
                Currencies.entries.random().name
            ),
            categories = categoriesToPreview
        )
    }

}

val categoriesToPreview = listOf(
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
