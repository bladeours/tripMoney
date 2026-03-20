package cc.n0th1ng.tripmoney.screens.addexpense

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.ExpenseDto
import cc.n0th1ng.tripmoney.screens.listexpense.CategorySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.DateTimePicker
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddExpenseBottomSheet(
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit,
    expenseDtoToEdit: ExpenseDto?,
    state: SheetState,
//    categories: List<Category> = emptyList()
) {
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
//    val currentTripId = 1
    val categories by expenseAndCategoryViewModel.getCategories().collectAsState(emptyList())
    if (categories.isEmpty()) {
        return
    }
    var amount by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.amount?.toString() ?: "0.00"
        )
    }
    val dummyFocusRequester = remember { FocusRequester() }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var currency by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.currency ?: Currencies.PLN.name
        )
    }
    var category by remember { mutableStateOf(expenseDtoToEdit?.category ?: categories[0]) }
    var datetime by remember {
        mutableStateOf(
            LocalDateTime.parse(
                expenseDtoToEdit?.expense?.datetime ?: LocalDateTime.now().toString()
            )
        )
    }
    var note by remember { mutableStateOf(expenseDtoToEdit?.expense?.note ?: "") }
    var enableSave by remember { mutableStateOf(expenseDtoToEdit != null) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
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
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                textAlign = TextAlign.Start
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = amount.ifEmpty { "0.00" },
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                CurrencyButton(onClick = { showCurrencyDialog = true }, text = currency)
            }
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDateTimePicker = true },
                    modifier = Modifier.weight(1f)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NoteInput(
                    note = note,
                    onTextChange = { newNote -> note = newNote },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    focusRequester = dummyFocusRequester
                )
            }

            Box(
                modifier = Modifier
                    .size(0.dp)
                    .focusRequester(dummyFocusRequester)
                    .focusable()
            )
            NumberKeyboard(
                onNumberClick = { number ->
                    val newText = (if (amount == "0.00") "" else amount) + number
                    if (newText.isDoubleTwoDigitsAboveZero()) {
                        amount = newText
                        enableSave = true
                    } else if (amount == "0.00") {
                        enableSave = false
                    }
                    dummyFocusRequester.requestFocus()
                },
                onBackspaceClick = {
                    if (amount == "0.00") return@NumberKeyboard
                    amount = amount.safeSubstring(0, amount.length - 1)
                    enableSave = amount.isDoubleTwoDigitsAboveZero()
                },
                onSave = {
                    val expenseToSave = Expense(
                        amount = amount.toDouble(),
                        currency = currency,
                        note = note,
                        datetime = datetime.toString(),
                        categoryId = category.id,
                        tripId = currentTripId
                    )
                    onSave(
                        if (expenseDtoToEdit == null) expenseToSave
                        else expenseToSave.copy(id = expenseDtoToEdit.expense.id)
                    )
                }, enableSave = enableSave
            )

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
    } catch (e: Exception) {
        "0.00"
    }
}

fun String.isDoubleTwoDigitsAboveZero(): Boolean {
    return this.toDoubleOrNull() != null && this.matches(Regex("^\\d*(\\.\\d{0,2})?$")) && this.toDouble() > 0
}

@Composable
fun NoteInput(note: String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier, focusRequester: FocusRequester) {
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
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(text)
    }
}

@Composable
fun CategoryButton(onClick: () -> Unit, category: Category, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.padding(end = 10.dp),
            painter = painterResource(category.icon.resource),
            contentDescription = stringResource(R.string.category),
            tint = Color(category.color.toColorInt())
        )
        Text(category.name, color = Color(category.color.toColorInt()))
    }
}

@Composable
fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
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
    onSave: () -> Unit,
    enableSave: Boolean
) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .aspectRatio(2f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = { onNumberClick("1") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("1", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("2") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("2", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("3") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("3", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("+", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = { onNumberClick("4") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("4", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("5") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("5", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("6") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("6", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("-", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = { onNumberClick("7") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("7", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("8") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("8", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("9") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("9", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("*", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = { onNumberClick(".") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text(".", fontSize = 20.sp)
            }
            TextButton(
                onClick = { onNumberClick("0") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("0", fontSize = 20.sp)
            }
            TextButton(
                onClick = onBackspaceClick,
                modifier = buttonModifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.backspace)
                )
            }
            TextButton(
                onClick = onSave,
                modifier = buttonModifier.weight(1f),
                enabled = enableSave
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.backspace)
                )
            }
        }
    }
}


//@SuppressLint("CoroutineCreationDuringComposition")
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview
//@Composable
//fun PreviewLight() {
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    CoroutineScope(Dispatchers.IO).launch {
//        sheetState.show()
//    }
//
//    TripMoneyTheme {
//        AddExpenseBottomSheet(
//            {}, {}, null, sheetState,
//            categories = listOf(
//                Category(
//                    name = "Hotel",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.HOTEL,
//                    color = "#B3E5FC"
//                ),
//                Category(
//                    name = "Jedzenie",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.RESTAURANT,
//                    color = "#C8E6C9"
//                ),
//                Category(
//                    name = "Transport",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.FLIGHT,
//                    color = "#FFCDD2"
//                ),
//                Category(
//                    name = "Rozrywka",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.ATTRACTION,
//                    color = "#FFF9C4"
//                ),
//                Category(
//                    name = "Zakupy",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#E1BEE7"
//                ),
//                Category(
//                    name = "Zakupy1",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#D7CCC8"
//                ),
//                Category(
//                    name = "Zakupy2",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#BBDEFB"
//                ),
//                Category(
//                    name = "Zakupy3",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#D1C4E9"
//                ),
//                Category(
//                    name = "Zakupy4",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#DCEDC8"
//                ),
//            )
//        )
//    }
//}
//
//@SuppressLint("CoroutineCreationDuringComposition")
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview
//@Composable
//fun PreviewDark() {
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    CoroutineScope(Dispatchers.IO).launch {
//        sheetState.show()
//    }
//
//    TripMoneyTheme(darkTheme = true) {
//        AddExpenseBottomSheet(
//            {}, {}, null, sheetState,
//            categories = listOf(
//                Category(
//                    name = "Hotel",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.HOTEL,
//                    color = "#B3E5FC"
//                ),
//                Category(
//                    name = "Jedzenie",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.RESTAURANT,
//                    color = "#C8E6C9"
//                ),
//                Category(
//                    name = "Transport",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.FLIGHT,
//                    color = "#FFCDD2"
//                ),
//                Category(
//                    name = "Rozrywka",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.ATTRACTION,
//                    color = "#FFF9C4"
//                ),
//                Category(
//                    name = "Zakupy",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#E1BEE7"
//                ),
//                Category(
//                    name = "Zakupy1",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#D7CCC8"
//                ),
//                Category(
//                    name = "Zakupy2",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#BBDEFB"
//                ),
//                Category(
//                    name = "Zakupy3",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#D1C4E9"
//                ),
//                Category(
//                    name = "Zakupy4",
//                    icon = cc.n0th1ng.tripmoney.utils.Icons.GROCERIES,
//                    color = "#DCEDC8"
//                ),
//            )
//        )
//    }
//}