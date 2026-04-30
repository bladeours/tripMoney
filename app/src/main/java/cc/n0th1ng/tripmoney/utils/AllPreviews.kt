package cc.n0th1ng.tripmoney.utils

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.n0th1ng.tripmoney.screens.addexpense.PreviewAddExpenseDisabled
import cc.n0th1ng.tripmoney.screens.addexpense.PreviewAddExpenseEnabled
import cc.n0th1ng.tripmoney.screens.settings.PreviewSettingsScreen
import cc.n0th1ng.tripmoney.screens.settings.SettingsScreen
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
annotation class AllPreviews