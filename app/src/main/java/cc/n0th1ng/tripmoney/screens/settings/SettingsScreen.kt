package cc.n0th1ng.tripmoney.screens.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R.*
import cc.n0th1ng.tripmoney.data.repository.AppTheme
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTheme by settingsViewModel.theme.collectAsState()
    val currentDefaultCurrency by settingsViewModel.defaultCurrency.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card {
            SettingsListItem(onClick = { showThemeDialog = true }, stringResource(string.theme)) {
                Text(
                    if (isSystemInDarkTheme()) stringResource(string.dark_theme) else stringResource(
                        string.light_theme
                    )
                )
            }
        }

        Card {
            SettingsListItem(
                onClick = { showCurrencyDialog = true },
                stringResource(string.default_currency)
            ) {
                 Text(currentDefaultCurrency.name)
            }
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { theme ->
                    settingsViewModel.setTheme(theme)
                    showThemeDialog = false
                },
                selected = currentTheme
            )
        }

        if (showCurrencyDialog) {
            CurrencySelectionDialog(onDismiss = {showCurrencyDialog = false}, onCurrencySelected = {
                currencyString ->
                settingsViewModel.setDefaultCurrency(Currencies.valueOf(currencyString))
                showCurrencyDialog = false
            }, currentDefaultCurrency.name)
        }
    }
}

@Composable
fun SettingsCard(@StringRes title: Int = -1, content: @Composable () -> Unit) {
    Card {
        if (title != -1) {
            Text(
                text = stringResource(title),
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(start = 15.dp, top = 15.dp, end = 15.dp)
                    .alpha(0.6f)
            )
        }
        content()
    }
}

@Composable
fun SettingsListItem(
    onClick: () -> Unit,
    headlineText: String,
    trailingContent: @Composable () -> Unit = {},
    supportingContent: @Composable () -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(headlineText) },
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        modifier = Modifier
            .clickable(true, onClick = onClick)
    )
}

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    selected: AppTheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(string.pick_theme)) },
        text = {
            Column {
                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeSelected(theme)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == theme,
                            onClick = {
                                onThemeSelected(theme)
                            }
                        )
                        Text(
                            text = when (theme) {
                                AppTheme.LIGHT -> stringResource(string.light_theme)
                                AppTheme.DARK -> stringResource(string.dark_theme)
                                AppTheme.SYSTEM -> stringResource(string.system_settings)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}