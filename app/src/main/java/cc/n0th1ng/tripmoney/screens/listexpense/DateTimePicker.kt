package cc.n0th1ng.tripmoney.screens.listexpense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
fun DateTimePicker(
    dateTime: LocalDateTime = LocalDateTime.now(),
    onChange: (LocalDateTime) -> Unit
) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = dateTime.toEpochMilli())
    val timePickerState = rememberTimePickerState(
        initialHour = dateTime.hour,
        initialMinute = dateTime.minute
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    val formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    OutlinedButton(onClick = { showDatePicker = true }) {
        Text(text = dateTime.format(formatter), fontSize = 17.sp)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        // open time picker next
                        showTimePicker = true
                        onChange(
                            LocalDateTime.of(
                                selectedDate,
                                dateTime.toLocalTime()
                            )
                        )
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onChange(LocalDateTime.of(dateTime.toLocalDate(), newTime))
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(string.cancel)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toEpochMilli(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()