package cc.n0th1ng.tripmoney.screens.addexpense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddExpenseBottomSheet(
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit,
    categories: List<Category>,
    expenseDtoToEdit: ExpenseDto?
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    var amount by remember {
        mutableStateOf(
            expenseDtoToEdit?.expense?.amount?.toString() ?: "0.00"
        )
    }
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = amount.ifEmpty { "0.00" },
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                CurrencyButton(onClick = { showCurrencyDialog = true }, text = currency)
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = { showDateTimePicker = true }) {
                Text(
                    text = datetime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                    fontSize = 17.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            CategoryButton(onClick = { showCategoryDialog = true }, category = category)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoteInput(note = note) { newNote -> note = newNote }
                SaveButton(
                    enabled = enableSave,
                    onClick = {
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
                    }
                )
            }
            Spacer(Modifier.height(14.dp))
            NumberKeyboard(
                onNumberClick = { number ->
                    val newText = (if (amount == "0.00") "" else amount) + number
                    if (newText.isDoubleTwoDigitsAboveZero()) {
                        amount = newText
                        enableSave = true
                    } else if (amount == "0.00") {
                        enableSave = false
                    }

                },
                onBackspaceClick = {
                    if (amount == "0.00") return@NumberKeyboard
                    amount = amount.safeSubstring(0, amount.length - 1)
                    enableSave = amount.isDoubleTwoDigitsAboveZero()
                })

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
fun NoteInput(note: String, onTextChange: (String) -> Unit) {
    var text by remember { mutableStateOf(note) }

    OutlinedTextField(
        label = { Text(stringResource(R.string.note)) }, value = note, onValueChange = { newText ->
            text = newText
            onTextChange(text)
        }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun CurrencyButton(onClick: () -> Unit, text: String) {
    OutlinedButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
fun CategoryButton(onClick: () -> Unit, category: Category) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.5f)
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

@Preview
@Composable
fun Preview() {
    TripMoneyTheme(darkTheme = true) {
        NumberKeyboard(onNumberClick = {}, onBackspaceClick = {})
    }
}


@Composable
fun NumberKeyboard(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit
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
            OutlinedButton(
                onClick = { onNumberClick("1") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("1", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("2") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("2", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("3") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("3", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = { onNumberClick("4") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("4", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("5") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("5", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("6") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("6", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = { onNumberClick("7") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("7", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("8") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("8", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("9") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("9", fontSize = 20.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = { onNumberClick(".") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text(".", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = { onNumberClick("0") },
                modifier = buttonModifier.weight(1f)
            ) {
                Text("0", fontSize = 20.sp)
            }
            OutlinedButton(
                onClick = onBackspaceClick,
                modifier = buttonModifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.backspace)
                )
            }
        }
    }
}