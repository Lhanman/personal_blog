package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.AdminViewModel

@Deprecated("默认管理端文章列表 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun AdminPostListScreen(
    viewModel: AdminViewModel,
    onCreatePost: () -> Unit,
    onEditPost: (Long) -> Unit
) {
    DeprecatedUiScreen("AdminPostListScreen")
}
