package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiComponent

@Deprecated("默认评论输入 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun CommentInput(
    isLoggedIn: Boolean,
    isPosting: Boolean,
    onPostComment: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    DeprecatedUiComponent("CommentInput")
}
