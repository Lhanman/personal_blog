package com.personalblog.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Deprecated("默认 Markdown 渲染 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    Text(text = content, modifier = modifier)
}
