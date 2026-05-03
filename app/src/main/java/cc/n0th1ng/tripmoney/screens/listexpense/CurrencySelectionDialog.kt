package cc.n0th1ng.tripmoney.screens.listexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import com.composables.icons.materialsymbols.outlined.R.drawable

@Composable
fun CurrencySelectionDialog(
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    selected: String
) {
    AlertDialog(
        modifier = Modifier.sizeIn(maxHeight = 500.dp),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_currency)) },
        text = {
            val scrollState = rememberLazyListState()
            val currencies = Currencies.names()
            var search by remember { mutableStateOf("") }

            LaunchedEffect(selected) {
                val index = currencies.indexOf(selected)
                if (index != -1) {
                    scrollState.animateScrollToItem(index)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { newText ->
                        search = newText
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(drawable.materialsymbols_ic_search_outlined),
                            contentDescription = "search"
                        )
                    }
                )

                val filteredCurrencies = if (search.isBlank()) {
                    currencies
                } else {
                    currencies.filter { currency ->
                        currency.lowercase().contains(search.lowercase())
                    }
                }

                LazyColumn(state = scrollState) {
                    items(
                        count = filteredCurrencies.size,
                        key = { index -> filteredCurrencies[index] }
                    ) { index ->
                        val currency = filteredCurrencies[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCurrencySelected(currency)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected == currency,
                                onClick = { onCurrencySelected(currency) }
                            )
                            Text(
                                text = currency,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = true,
                onClick = onDismiss,
            ) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@AllPreviews
@Composable
fun PreviewCurrencySelectionDialog() {
    TripMoneyTheme {
        CurrencySelectionDialog(
            {},
            {},
            Currencies.names().random()
        )
    }
}