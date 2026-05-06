package cc.n0th1ng.tripmoney.screens.listexpense

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val datePickerState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate.toEpochMilli(),
            initialSelectedEndDateMillis = endDate.toEpochMilli()
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedStartDateMillis = datePickerState.selectedStartDateMillis
                val selectedEndDateMillis = datePickerState.selectedEndDateMillis
                if (selectedStartDateMillis != null && selectedEndDateMillis != null) {
                    val selectedStartDate = Instant.ofEpochMilli(selectedStartDateMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val selectedEndDate =
                        Instant.ofEpochMilli(selectedEndDateMillis).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    onConfirm(selectedStartDate, selectedEndDate)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(string.cancel)) }
        }
    ) {
        DateRangePicker(
            state = datePickerState, showModeToggle = false,
            title = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    date: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilli())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row() {
                TextButton(onClick = {
                        onConfirm(LocalDate.now().minusDays(1))
                }) {
                    Text(stringResource(string.yesterday))
                }
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
fun TimePicker(
    onDismiss: () -> Unit,
    onConfirm: (TimePickerState) -> Unit,
    time: LocalTime = LocalTime.now()
) {
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
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
@OptIn(ExperimentalMaterial3Api::class)
fun DateTimePicker(
    dateTime: LocalDateTime = LocalDateTime.now(),
    onChange: (LocalDateTime) -> Unit
) {

    var showDatePicker by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(dateTime.toLocalDate()) }

    if (showDatePicker) {
        DatePicker(
            date = dateTime.toLocalDate(),
            onDismiss = { showDatePicker = false }, onConfirm = { newDate ->
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
        }, time = dateTime.toLocalTime())
    }
}

fun LocalDateTime.toEpochMilli(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.toEpochMilli(): Long =
    this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

@AllPreviews
@Composable
fun DatePickerPreview() {
    TripMoneyTheme {
        DatePicker(LocalDate.now(), {}, {})
    }
}