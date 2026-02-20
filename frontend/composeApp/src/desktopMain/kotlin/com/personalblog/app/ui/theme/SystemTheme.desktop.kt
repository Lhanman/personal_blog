package com.personalblog.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isSystemInDarkMode(): Boolean = isSystemInDarkTheme()
