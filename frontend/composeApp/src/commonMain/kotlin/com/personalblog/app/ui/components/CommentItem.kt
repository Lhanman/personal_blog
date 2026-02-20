package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiComponent
import com.personalblog.shared.dto.CommentDto

@Deprecated("默认评论项 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun CommentItem(comment: CommentDto) {
    DeprecatedUiComponent("CommentItem")
}
