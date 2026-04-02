package cc.n0th1ng.tripmoney.screens.trippicker

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.screens.addexpense.CurrencyButton
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.DatePicker
import cc.n0th1ng.tripmoney.screens.listexpense.DateRangePicker
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.pretty
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import io.ktor.http.hostIsIp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit,
    tripToEdit: Trip?,
    sheetState: SheetState
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val defaultCurrency by settingsViewModel.defaultCurrency.collectAsState()

    AddTripBottomSheet(
        onDismiss = onDismiss,
        onSave = onSave,
        tripToEdit = tripToEdit,
        sheetState = sheetState,
        defaultCurrency = defaultCurrency
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit,
    tripToEdit: Trip?,
    sheetState: SheetState,
    defaultCurrency: Currencies
) {

    var name by remember { mutableStateOf(tripToEdit?.name ?: "") }
    var startDate by remember {
        mutableStateOf(
            tripToEdit?.startDate ?: LocalDate.now()
        )
    }

    var endDate by remember {
        mutableStateOf(
            tripToEdit?.startDate ?: LocalDate.now()
        )
    }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var budgetString by remember { mutableStateOf(tripToEdit?.budget?.toString() ?: "") }
    var currency by remember { mutableStateOf(tripToEdit?.currency ?: defaultCurrency.name) }
    var enableSave by remember { mutableStateOf(tripToEdit != null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp),
                text = stringResource(if (tripToEdit == null) R.string.add_trip else R.string.edit_trip),
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                textAlign = TextAlign.Start
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            NameInput(name = name, onTextChange = { newText ->
                name = newText
                enableSave = !name.isEmpty()
            })
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BudgetInput(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    budget = budgetString,
                    onTextChange = { newBudget -> budgetString = newBudget })
                CurrencyButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(1f),
                    onClick = { showCurrencyDialog = true }, text = currency
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { showDatePicker = true }) {
                    val startDateFormatted = startDate.pretty()
                    val endDateFormatted = endDate.pretty()
                    Text(
                        text = "$startDateFormatted - $endDateFormatted",
                        fontSize = 17.sp
                    )
                }


            }

            Button(
                modifier = Modifier.fillMaxWidth(0.9f),
                enabled = enableSave,
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    val trip =
                        Trip(
                            name = name,
                            startDate = startDate,
                            endDate = endDate,
                            currency = currency,
                            budget = budgetString.toDoubleOrNull() ?: 0.0
                        )

                    onSave(if (tripToEdit == null) trip else trip.copy(id = tripToEdit.id))
                }) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.save)
                )
            }
            Spacer(Modifier.height(5.dp))
        }
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

    if (showDatePicker) {
        DateRangePicker(
            startDate = startDate,
            endDate = endDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { newStartDate, newEndDate ->
                startDate = newStartDate
                endDate = newEndDate
                showDatePicker = false
            })
    }
}

@Composable
fun NameInput(name: String, onTextChange: (String) -> Unit) {
    var text by remember { mutableStateOf(name) }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(0.9f),
        label = { Text(stringResource(R.string.name)) }, value = name, onValueChange = { newText ->
            text = newText
            onTextChange(text)
        }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun BudgetInput(modifier: Modifier = Modifier, budget: String, onTextChange: (String) -> Unit) {
    var text by remember { mutableStateOf(budget) }
    OutlinedTextField(
        placeholder = { Text("0.0") },
        modifier = modifier,
        label = { Text(stringResource(R.string.budget)) },
        value = text,
        onValueChange = { newText ->
            val regex = Regex("^\\d*\\.?\\d{0,2}$")
            if (regex.matches(newText)) {
                text = newText
                onTextChange(text)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@AllPreviews
@Composable
fun PreviewAddTripBottomSheet() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    CoroutineScope(Dispatchers.IO).launch {
        sheetState.show()
    }
    TripMoneyTheme {
        AddTripBottomSheet({}, {}, null, sheetState, defaultCurrency = Currencies.entries.random())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@AllPreviews
@Composable
fun PreviewAddTripBottomSheetEditTrip() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    CoroutineScope(Dispatchers.IO).launch {
        sheetState.show()
    }
    TripMoneyTheme {
        AddTripBottomSheet(
            {},
            {},
            Trip(1, "Włochy", LocalDate.parse("2025-01-02"),
                LocalDate.parse("2025-01-15"), "PLN", budget = 0.0),
            sheetState,
            defaultCurrency = Currencies.entries.random()
        )
    }
}