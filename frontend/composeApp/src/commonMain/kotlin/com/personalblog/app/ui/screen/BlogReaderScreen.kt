package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.BlogReaderViewModel
import com.personalblog.app.ui.viewmodel.CommentViewModel

@Deprecated("默认博客阅读 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun BlogReaderScreen(
    viewModel: BlogReaderViewModel,
    commentViewModel: CommentViewModel,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    DeprecatedUiScreen("BlogReaderScreen")
}
