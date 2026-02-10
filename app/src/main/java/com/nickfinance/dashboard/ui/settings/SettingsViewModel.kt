package com.nickfinance.dashboard.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickfinance.dashboard.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        val THEME_MODE_KEY = intPreferencesKey("theme_mode") // 0=DARK, 1=LIGHT, 2=SYSTEM
        val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
        val SAVINGS_TARGET_KEY = doublePreferencesKey("savings_target")
        val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
    }

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _currencySymbol = MutableStateFlow("¥")
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _savingsTarget = MutableStateFlow(80000.0)
    val savingsTarget: StateFlow<Double> = _savingsTarget

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    init {
        viewModelScope.launch {
            context.dataStore.data.map { prefs ->
                val modeInt = prefs[THEME_MODE_KEY] ?: 1
                ThemeMode.entries.getOrElse(modeInt) { ThemeMode.LIGHT }
            }.collect { _themeMode.value = it }
        }
        viewModelScope.launch {
            context.dataStore.data.map { prefs ->
                prefs[CURRENCY_SYMBOL_KEY] ?: "¥"
            }.collect { _currencySymbol.value = it }
        }
        viewModelScope.launch {
            context.dataStore.data.map { prefs ->
                prefs[SAVINGS_TARGET_KEY] ?: 80000.0
            }.collect { _savingsTarget.value = it }
        }
        viewModelScope.launch {
            context.dataStore.data.map { prefs ->
                prefs[ONBOARDING_DONE_KEY] ?: false
            }.collect { _onboardingDone.value = it }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = mode.ordinal
            }
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[CURRENCY_SYMBOL_KEY] = symbol
            }
        }
    }

    fun setSavingsTarget(target: Double) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[SAVINGS_TARGET_KEY] = target
            }
        }
    }

    fun setOnboardingDone() {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[ONBOARDING_DONE_KEY] = true
            }
        }
    }
}
