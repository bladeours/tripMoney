package cc.n0th1ng.tripmoney.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cc.n0th1ng.tripmoney.data.repository.PreferenceKeys.APP_THEME
import cc.n0th1ng.tripmoney.data.repository.PreferenceKeys.CURRENT_TRIP
import cc.n0th1ng.tripmoney.data.repository.PreferenceKeys.DEFAULT_CURRENCY
import cc.n0th1ng.tripmoney.utils.Currencies
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import javax.inject.Inject


val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

object PreferenceKeys {
    val APP_THEME = intPreferencesKey("app_theme")
    val CURRENT_TRIP = intPreferencesKey("current_trip")
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")

}

class PreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {
    val themeFlow: Flow<AppTheme> =
        context.preferencesDataStore.data.map { prefs ->
            val value = prefs[APP_THEME]
                ?: AppTheme.SYSTEM.value
            AppTheme.fromValue(value)
        }

    val currentTripFlow: Flow<Int> =
        context.preferencesDataStore.data.map { prefs ->
            prefs[CURRENT_TRIP] ?: -1
        }

    val defaultCurrencyFlow: Flow<Currencies> =
        context.preferencesDataStore.data.map { prefs ->
            Currencies.valueOf(prefs[DEFAULT_CURRENCY] ?: Currencies.default().name)
        }

    suspend fun saveDefaultCurrency(currency: Currencies) {
        context.preferencesDataStore.edit { prefs ->
            prefs[DEFAULT_CURRENCY] = currency.name
        }
    }
    suspend fun saveCurrentTrip(tripId: Int) {
        context.preferencesDataStore.edit { prefs ->
            prefs[CURRENT_TRIP] = tripId
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.preferencesDataStore.edit { prefs ->
            prefs[APP_THEME] = theme.value
        }
    }

}

enum class AppTheme(val value: Int) {
    LIGHT(0), DARK(1), SYSTEM(2);

    companion object {
        fun fromValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}