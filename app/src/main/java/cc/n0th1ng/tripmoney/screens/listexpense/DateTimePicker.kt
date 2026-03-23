package cc.n0th1ng.tripmoney.screens.listexpense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import cc.n0th1ng.tripmoney.R.*
import java.sql.Time
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    dateTime: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = dateTime.toEpochMilli())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null) {
                    val selectedDate = Instant.ofEpochMilli(selectedMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onConfirm(selectedDate)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(string.cancel)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(onDismiss: () -> Unit, onConfirm: (TimePickerState) -> Unit) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(string.cancel)) }
        },
        text = { TimePicker(state = timePickerState) }
    )

}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
fun DateTimePicker(
    dateTime: LocalDateTime = LocalDateTime.now(),
    onChange: (LocalDateTime) -> Unit
) {

    var showDatePicker by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(dateTime.toLocalDate()) }

    if (showDatePicker) {
        DatePicker(onDismiss = { showDatePicker = false }, onConfirm = { newDate ->
            date = newDate
            showDatePicker = false
            showTimePicker = true
        })
    }

    if (showTimePicker) {
        TimePicker(onDismiss = {
            showTimePicker = false
            showDatePicker = true
        }, onConfirm = { timePickerState ->
            showTimePicker = false
            showDatePicker = true
            val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
            onChange(LocalDateTime.of(date, newTime))
        })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toEpochMilli(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toEpochMilli(): Long =
    this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()