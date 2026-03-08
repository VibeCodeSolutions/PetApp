package com.example.tierapp.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    val uiState = dataStore.data.map { prefs ->
        SettingsUiState(
            themeMode = prefs[KEY_THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_THEME_MODE] = mode.name }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
        }
    }
}
