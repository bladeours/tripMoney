package cc.n0th1ng.tripmoney.screens.settings

import android.content.Intent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R.*
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.data.repository.AppTheme
import cc.n0th1ng.tripmoney.screens.listexpense.CurrencySelectionDialog
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.saveCsv
import cc.n0th1ng.tripmoney.utils.shareCsv
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import com.composables.icons.materialsymbols.outlined.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentTheme by settingsViewModel.theme.collectAsState()
    val currentDefaultCurrency by settingsViewModel.defaultCurrency.collectAsState()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val context = LocalContext.current
    val tripName = currentTrip?.name ?: ""
    val scope = rememberCoroutineScope()

    SettingsScreen(
        currentDefaultCurrency = currentDefaultCurrency,
        currentTheme = currentTheme,
        onThemeSave = { settingsViewModel.setTheme(it) },
        onCurrencySave = { settingsViewModel.setDefaultCurrency(it) },
        tripName = tripName,
        onExportToCsv = {
            scope.launch {
                try {
                    val safeTripName = tripName.replace(Regex("[^a-zA-Z0-9_]"), "_")
                    val file = File(context.cacheDir, "$safeTripName.csv")
                    expenseAndCategoryViewModel.generateCSVToFile(currentTripId, file)
                    shareCsv(context, file)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SettingsScreen(
    currentDefaultCurrency: Currencies,
    currentTheme: AppTheme,
    onThemeSave: (AppTheme) -> Unit,
    onCurrencySave: (Currencies) -> Unit,
    tripName: String,
    onExportToCsv: () -> Unit,
) {

    Scaffold { padding ->
        var showThemeDialog by remember { mutableStateOf(false) }
        var showCurrencyDialog by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsListItem(
                onClick = { showCurrencyDialog = true },
                headlineText = stringResource(string.default_currency),
                supportingText = currentDefaultCurrency.name,
                iconResource = R.drawable.materialsymbols_ic_currency_yen_outlined
            )

            SettingsCard(string.theme) {
                SettingsListItem(
                    onClick = { showThemeDialog = true },
                    stringResource(string.theme),
                    supportingText = if (isSystemInDarkTheme()) stringResource(string.dark_theme) else stringResource(
                        string.light_theme
                    ),
                    iconResource = R.drawable.materialsymbols_ic_format_paint_outlined
                )
                SettingsListItem(
                    onClick = { },
                    "Pallete",
                    supportingText = if (isSystemInDarkTheme()) stringResource(string.dark_theme) else stringResource(
                        string.light_theme
                    ),
                    iconResource = R.drawable.materialsymbols_ic_palette_outlined
                )
            }
            SettingsListItem(
                onClick = onExportToCsv,
                stringResource(string.export_to_csv),
                supportingText = "Save expenses from %s to a file".format(tripName),
                iconResource = R.drawable.materialsymbols_ic_csv_outlined
            )

            if (showThemeDialog) {
                ThemeSelectionDialog(
                    onDismiss = { showThemeDialog = false },
                    onThemeSelected = { theme ->
                        onThemeSave(theme)
                        showThemeDialog = false
                    },
                    selected = currentTheme
                )
            }

            if (showCurrencyDialog) {
                CurrencySelectionDialog(
                    onDismiss = { showCurrencyDialog = false },
                    onCurrencySelected = { currencyString ->
                        onCurrencySave(Currencies.valueOf(currencyString))
                        showCurrencyDialog = false
                    },
                    currentDefaultCurrency.name
                )
            }
        }
    }
}

@Composable
fun SettingsCard(@StringRes title: Int = -1, content: @Composable () -> Unit) {
    Card {
        if (title != -1) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleSmall,
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
    supportingText: String,
    iconResource: Int
) {
    Card {
        ListItem(
            leadingContent = {
                Icon(painter = painterResource(iconResource), contentDescription = null)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(headlineText) },
            supportingContent = { Text(supportingText) },
            trailingContent = trailingContent,
            modifier = Modifier
                .clickable(true, onClick = onClick)
        )
    }
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

@RequiresApi(Build.VERSION_CODES.S)
@AllPreviews
@Composable
fun PreviewSettingsScreen() {
    TripMoneyTheme {
        SettingsScreen(Currencies.entries.random(), AppTheme.entries.random(), {}, {}, "Włochy", {})
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@AllPreviews
@Composable
fun PreviewThemeSelectionDialog() {
    TripMoneyTheme {
        ThemeSelectionDialog(onDismiss = {}, onThemeSelected = {}, AppTheme.SYSTEM)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@AllPreviews
@Composable
fun PreviewCurrencySelectionDialog() {
    TripMoneyTheme {
        CurrencySelectionDialog(
            onDismiss = {},
            onCurrencySelected = {},
            selected = Currencies.entries.random().name
        )
    }
}