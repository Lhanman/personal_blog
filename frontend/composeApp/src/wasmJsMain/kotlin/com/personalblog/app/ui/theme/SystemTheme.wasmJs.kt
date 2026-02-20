package com.personalblog.app.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun isSystemInDarkMode(): Boolean {
    return false // Web 默认浅色模式，可以后续通过 JS 互操作实现
}
