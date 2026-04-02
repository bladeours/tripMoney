package cc.n0th1ng.tripmoney.viewmodel

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.n0th1ng.tripmoney.data.repository.AppTheme
import cc.n0th1ng.tripmoney.data.repository.PreferencesRepository
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: PreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val uiModeManager: UiModeManager =
        context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    val theme = repo.themeFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppTheme.SYSTEM
        )

    val autoOpenStartupPref = repo.currentAddExpenseSwitchFlow
        .take(1)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val addExpenseSwitch = repo.currentAddExpenseSwitchFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val currentTrip = repo.currentTripFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        -1
    )

    val defaultCurrency = repo.defaultCurrencyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        Currencies.default()
    )

    fun setDefaultCurrency(currency: Currencies) {
        viewModelScope.launch {
            repo.saveDefaultCurrency(currency)
        }
    }

    fun setCurrentAddExpenseSwitch(value: Boolean) {
        viewModelScope.launch {
            repo.saveAddExpenseSwitch(value)
        }
    }
    fun setCurrentTrip(tripId: Int) {
        viewModelScope.launch {
            repo.saveCurrentTrip(tripId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setTheme(theme: AppTheme) {
        applyTheme(theme)

        viewModelScope.launch {
            repo.saveTheme(theme)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.LIGHT ->
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)

            AppTheme.DARK ->
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)

            AppTheme.SYSTEM ->
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
        }
    }
}

