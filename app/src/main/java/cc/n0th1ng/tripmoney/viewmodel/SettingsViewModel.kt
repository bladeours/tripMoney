package cc.n0th1ng.tripmoney.viewmodel

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cc.n0th1ng.tripmoney.data.repository.AppTheme
import cc.n0th1ng.tripmoney.data.repository.PreferencesRepository
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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



