package cc.n0th1ng.tripmoney.screens.trippicker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.screens.addexpense.CurrencyButton
import cc.n0th1ng.tripmoney.screens.addexpense.isDoubleTwoDigitsAboveZero
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.screens.listexpense.DatePicker
import cc.n0th1ng.tripmoney.screens.listexpense.DateTimePicker
import cc.n0th1ng.tripmoney.utils.Currencies
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripBottomSheet(onDismiss: () -> Unit, onSave: (Trip) -> Unit, tripToEdit: Trip?) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(tripToEdit?.name ?: "") }
    var startDate by remember {
        mutableStateOf(
            LocalDate.parse(tripToEdit?.startDate ?: LocalDate.now().toString())
        )
    }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currency by remember { mutableStateOf(tripToEdit?.currency ?: Currencies.default().name) }
    var enableSave by remember { mutableStateOf(tripToEdit != null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            NameInput(name = name, onTextChange = { newText ->
                name = newText
                enableSave = !name.isEmpty()
            })
            CurrencyButton(onClick = {showCurrencyDialog = true}, currency)
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(
                    text = startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    fontSize = 17.sp
                )
            }
            OutlinedButton(
                enabled = enableSave,
                onClick = {
                onSave(Trip(name = name, startDate = startDate.toString(), currency = currency))
            }) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.save)
                )
            }
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
        DatePicker(startDate, onDismiss = {showDatePicker = false}, onConfirm = { newDate ->
            startDate = newDate
            showDatePicker = false
        })
    }
}

@Composable
fun NameInput(name: String, onTextChange: (String) -> Unit) {
    var text by remember { mutableStateOf(name) }
    OutlinedTextField(
        label = { Text(stringResource(R.string.name)) }, value = name, onValueChange = { newText ->
            text = newText
            onTextChange(text)
        }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

