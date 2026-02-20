package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.personalblog.app.ui.DeprecatedUiComponent

@Deprecated("默认 Markdown 编辑器 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun AdminMarkdownEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DeprecatedUiComponent("AdminMarkdownEditor", modifier)
}
