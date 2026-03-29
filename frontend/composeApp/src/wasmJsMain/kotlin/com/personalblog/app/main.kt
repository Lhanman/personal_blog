package com.personalblog.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.personalblog.app.logging.LoggingRuntime

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    LoggingRuntime.initialize()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        PersonalBlog()
    }
}
