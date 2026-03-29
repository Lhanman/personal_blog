package com.personalblog.app.ui.theme

import androidx.compose.runtime.Composable

@JsFun("() => window.matchMedia('(prefers-color-scheme: dark)').matches")
private external fun jsSystemIsDark(): JsBoolean

@Composable
actual fun isSystemInDarkMode(): Boolean = jsSystemIsDark().toBoolean()
