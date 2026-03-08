package com.example.tierapp.feature.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
)
