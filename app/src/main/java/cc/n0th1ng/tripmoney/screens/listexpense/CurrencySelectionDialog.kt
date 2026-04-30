package cc.n0th1ng.tripmoney.screens.listexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.utils.Currencies

@Composable
fun CurrencySelectionDialog(
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    selected: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_currency)) },
        text = {
            Column {
                Currencies.names().forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCurrencySelected(currency)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selected == currency, onClick = {
                                onCurrencySelected(currency)
                            })
                        Text(
                            text = currency, modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {})
}