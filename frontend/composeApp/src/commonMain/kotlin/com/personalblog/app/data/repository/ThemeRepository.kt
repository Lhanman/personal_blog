package com.personalblog.app.data.repository

import com.personalblog.app.ui.theme.ThemeMode
import com.russhwolf.settings.Settings

class ThemeRepository(private val settings: Settings) {
    companion object {
        private const val KEY_THEME = "theme_mode"
    }

    fun getThemeMode(): ThemeMode {
        val value = settings.getStringOrNull(KEY_THEME) ?: return ThemeMode.DARK
        return try {
            ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ThemeMode.DARK
        }
    }

    fun saveThemeMode(mode: ThemeMode) {
        settings.putString(KEY_THEME, mode.name)
    }
}
