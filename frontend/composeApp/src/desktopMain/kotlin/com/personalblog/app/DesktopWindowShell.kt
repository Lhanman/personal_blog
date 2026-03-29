package com.personalblog.app

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.personalblog.app.ui.navigation.AppWindowChrome
import java.awt.Frame

@Composable
fun FrameWindowScope.DesktopWindowShell(
    desktopPlatform: DesktopPlatform,
    windowState: WindowState,
    onCloseRequest: () -> Unit,
    content: @Composable (AppWindowChrome) -> Unit
) {
    ConfigureDesktopWindowForPlatform(desktopPlatform)
    content(
        rememberDesktopWindowChrome(
            desktopPlatform = desktopPlatform,
            windowState = windowState,
            onCloseRequest = onCloseRequest
        )
    )
}

@Composable
private fun FrameWindowScope.ConfigureDesktopWindowForPlatform(
    desktopPlatform: DesktopPlatform
) {
    DisposableEffect(window, desktopPlatform) {
        if (desktopPlatform == DesktopPlatform.MacOs) {
            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }
        onDispose { }
    }
}

@Composable
fun FrameWindowScope.rememberDesktopWindowChrome(
    desktopPlatform: DesktopPlatform,
    windowState: WindowState,
    onCloseRequest: () -> Unit
): AppWindowChrome {
    val isMacOs = desktopPlatform == DesktopPlatform.MacOs
    return AppWindowChrome(
        immersiveHeader = true,
        dragArea = { modifier, content ->
            WindowDraggableArea(modifier = modifier, content = content)
        },
        windowActions = if (isMacOs) {
            null
        } else {
            {
                DesktopWindowActions(
                    isMaximized = windowState.placement == WindowPlacement.Maximized,
                    onMinimize = { window.extendedState = window.extendedState or Frame.ICONIFIED },
                    onToggleMaximize = {
                        windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Maximized
                        }
                    },
                    onCloseRequest = onCloseRequest
                )
            }
        },
        titleBarStartInset = if (isMacOs) 76.dp else 0.dp,
        titleBarTopInset = if (isMacOs) 8.dp else 0.dp
    )
}

@Composable
private fun RowScope.DesktopWindowActions(
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onCloseRequest: () -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurface

    IconButton(onClick = onMinimize) {
        Text(
            text = "—",
            color = iconTint,
            style = MaterialTheme.typography.titleMedium
        )
    }

    IconButton(onClick = onToggleMaximize) {
        Text(
            text = if (isMaximized) "❐" else "□",
            color = iconTint,
            style = MaterialTheme.typography.labelLarge
        )
    }

    IconButton(onClick = onCloseRequest) {
        Text(
            text = "✕",
            color = iconTint,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
