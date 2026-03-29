package com.personalblog.app

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.personalblog.app.logging.LoggingRuntime

fun main() = application {
    LoggingRuntime.initialize()
    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
    val desktopPlatform = remember { currentDesktopPlatform() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Personal Blog",
        state = windowState,
        undecorated = desktopPlatform != DesktopPlatform.MacOs
    ) {
        DesktopWindowShell(
            desktopPlatform = desktopPlatform,
            windowState = windowState,
            onCloseRequest = ::exitApplication
        ) { windowChrome ->
            PersonalBlog(windowChrome = windowChrome)
        }
    }
}
