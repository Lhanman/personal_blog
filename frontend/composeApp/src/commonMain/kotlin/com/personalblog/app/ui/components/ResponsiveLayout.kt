package com.personalblog.app.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    mobileContent: @Composable () -> Unit,
    desktopContent: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 768.dp) {
            mobileContent()
        } else {
            desktopContent()
        }
    }
}
