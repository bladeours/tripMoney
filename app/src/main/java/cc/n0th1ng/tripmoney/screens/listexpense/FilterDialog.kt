package cc.n0th1ng.tripmoney.screens.listexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.n0th1ng.tripmoney.Filter
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.navigation.AmountTextField
import cc.n0th1ng.tripmoney.screens.addexpense.categoriesToPreview
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.pretty
import java.time.LocalDate

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onSave: (Filter) -> Unit,
    onClear: () -> Unit,
    categories: List<Category>,
    filter: Filter
) {
    var filter by remember { mutableStateOf(filter) }
    var fromAmountString by remember { mutableStateOf(filter.startAmount.toString()) }
    var toAmountString by remember { mutableStateOf(filter.endAmount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = true,
                onClick = onClear
            ) { Text(stringResource(R.string.clear)) }
        },
        confirmButton = {
            Button(
                enabled = true,
                onClick = {
                    onSave(filter)
                }) { Text(stringResource(R.string.save)) }
        }, title = { Text("Filter") },
        text = {
            var showDatePicker by remember { mutableStateOf(false) }
            var startDate by remember {
                mutableStateOf(filter.startDate)
            }
            var endDate by remember {
                mutableStateOf(filter.endDate)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Categories")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier
                        .sizeIn(maxHeight = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    categories.forEach {
                        FilterChip(selected = filter.categories.contains(it), onClick = {
                            filter = if (filter.categories.contains(it)) {
                                filter.without(it)
                            } else {
                                filter.with(it)
                            }
                        }, label = {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(painterResource(it.icon.resource), contentDescription = null)
                                Text(text = it.name)
                            }
                        })
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(1f),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { showDatePicker = true }) {
                    val startDateFormatted = startDate.pretty()
                    val endDateFormatted = endDate.pretty()
                    Text(
                        text =
                            if(startDate == LocalDate.MIN && endDate == LocalDate.MAX) "Show all dates" else
                            "$startDateFormatted - $endDateFormatted",
                        fontSize = 17.sp
                    )
                }
                AmountTextField(label = "from", onValueChange = { newText ->
                    fromAmountString = newText
                    filter = filter.withStartAmount(newText.safeToDouble())
                }, value = fromAmountString)
                AmountTextField(label = "to", onValueChange = { newText ->
                    toAmountString = newText
                    filter = filter.withEndAmount(newText.safeToDouble())
                }, value = toAmountString)
            }

            if (showDatePicker) {
                DateRangePicker(
                    startDate = if(startDate == LocalDate.MIN) LocalDate.now() else startDate,
                    endDate = if(endDate == LocalDate.MAX) LocalDate.now() else endDate,
                    onDismiss = { showDatePicker = false },
                    onConfirm = { newStartDate, newEndDate ->
                        startDate = newStartDate
                        endDate = newEndDate
                        filter = filter.withStartDate(startDate).withEndDate(endDate)
                        showDatePicker = false
                    })
            }
        })
}


@AllPreviews
@Composable
fun PreviewFilterDialog() {
    TripMoneyTheme {
        FilterDialog(
            onDismiss = {},
            onSave = {},
            categories = categoriesToPreview.plus(categoriesToPreview).plus(categoriesToPreview),
            filter = Filter(),
            onClear = {}
        )
    }
}


private fun String.safeToDouble(): Double {
    if (this == "∞") return Double.MAX_VALUE
    if (this.isEmpty()) return 0.0
    return this.toDouble()
}