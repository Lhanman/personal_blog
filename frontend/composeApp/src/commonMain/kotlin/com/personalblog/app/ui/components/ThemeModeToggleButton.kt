package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.personalblog.app.ui.DeprecatedUiComponent
import com.personalblog.app.ui.theme.ThemeMode

@Deprecated("默认主题切换 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun ThemeModeToggleButton(
    currentMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    DeprecatedUiComponent("ThemeModeToggleButton", modifier)
}
