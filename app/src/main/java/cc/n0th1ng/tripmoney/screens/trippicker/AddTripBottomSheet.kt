package cc.n0th1ng.tripmoney.screens.trippicker

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import io.ktor.http.hostIsIp
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

    var name by remember { mutableStateOf(tripToEdit?.name ?: "") }
    var startDate by remember {
        mutableStateOf(
            LocalDate.parse(tripToEdit?.startDate ?: LocalDate.now().toString())
        )
    }
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val defaultCurrency by settingsViewModel.defaultCurrency.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
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
                text = stringResource(if(tripToEdit == null) R.string.add_trip else R.string.edit_trip),
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CurrencyButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(1f),
                    onClick = { showCurrencyDialog = true }, text = currency
                )
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .weight(1f),
                    onClick = { showDatePicker = true }) {
                    Text(
                        text = startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        fontSize = 17.sp
                    )
                }

            }

            Button(
                modifier = Modifier.fillMaxWidth(0.9f),
                enabled = enableSave,
                onClick = {
                    val trip = Trip(name = name, startDate = startDate.toString(), currency = currency)

                    onSave(if(tripToEdit == null) trip else trip.copy(id = tripToEdit.id))
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
        DatePicker(startDate, onDismiss = { showDatePicker = false }, onConfirm = { newDate ->
            startDate = newDate
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