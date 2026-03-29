package com.personalblog.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ContentWidthStyle {
    Reading,
    Page,
    Wide
}

@Composable
fun ContentContainer(
    modifier: Modifier = Modifier,
    widthStyle: ContentWidthStyle = ContentWidthStyle.Page,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontalPadding = when {
            maxWidth >= 1440.dp -> 40.dp
            maxWidth >= 1024.dp -> 28.dp
            else -> 16.dp
        }

        Box(
            modifier = Modifier
                .widthIn(max = widthStyle.resolveMaxWidth(maxWidth))
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            content()
        }
    }
}

private fun ContentWidthStyle.resolveMaxWidth(availableWidth: Dp): Dp = when (this) {
    ContentWidthStyle.Reading -> when {
        availableWidth >= 1600.dp -> 940.dp
        availableWidth >= 1200.dp -> 880.dp
        else -> availableWidth
    }

    ContentWidthStyle.Page -> when {
        availableWidth >= 1600.dp -> 1320.dp
        availableWidth >= 1200.dp -> 1140.dp
        availableWidth >= 900.dp -> 960.dp
        else -> availableWidth
    }

    ContentWidthStyle.Wide -> when {
        availableWidth >= 1800.dp -> 1520.dp
        availableWidth >= 1400.dp -> 1360.dp
        availableWidth >= 1100.dp -> 1180.dp
        else -> availableWidth
    }
}
