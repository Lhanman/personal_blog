package com.personalblog.app.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

typealias HeaderDragArea = @Composable (modifier: Modifier, content: @Composable () -> Unit) -> Unit
typealias HeaderWindowActions = @Composable RowScope.() -> Unit

data class AppWindowChrome(
    val immersiveHeader: Boolean = false,
    val dragArea: HeaderDragArea? = null,
    val windowActions: HeaderWindowActions? = null,
    val titleBarStartInset: Dp = 0.dp,
    val titleBarTopInset: Dp = 0.dp
)

data class AppHeaderState(
    val currentRoute: String?,
    val screenChrome: ScreenChrome,
    val canNavigateBack: Boolean
) {
    val showBackButton: Boolean
        get() = !screenChrome.isTopLevel && (canNavigateBack || screenChrome.parentRoute != null)

    val fallbackRoute: String?
        get() = screenChrome.parentRoute
}
