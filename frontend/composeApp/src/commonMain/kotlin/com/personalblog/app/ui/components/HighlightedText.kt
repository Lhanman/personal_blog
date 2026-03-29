package com.personalblog.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (query.isBlank()) {
        Text(text = text, modifier = modifier, maxLines = maxLines, overflow = overflow)
        return
    }

    val highlightColor = MaterialTheme.colorScheme.primary
    val annotated = buildAnnotatedString {
        var start = 0
        val lower = text.lowercase()
        val queryLower = query.lowercase()
        while (true) {
            val idx = lower.indexOf(queryLower, start)
            if (idx < 0) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, idx))
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                append(text.substring(idx, idx + query.length))
            }
            start = idx + query.length
        }
    }
    Text(text = annotated, modifier = modifier, maxLines = maxLines, overflow = overflow)
}
