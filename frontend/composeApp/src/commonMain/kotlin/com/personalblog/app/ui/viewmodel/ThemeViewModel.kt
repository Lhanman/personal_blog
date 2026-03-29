package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.personalblog.app.data.repository.ThemeRepository
import com.personalblog.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeState(
    val mode: ThemeMode = ThemeMode.DARK
)

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ThemeState(mode = themeRepository.getThemeMode()))
    val state: StateFlow<ThemeState> = _state.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        themeRepository.saveThemeMode(mode)
        _state.value = ThemeState(mode = mode)
    }

    fun cycleThemeMode() {
        val next = when (_state.value.mode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(next)
    }
}
