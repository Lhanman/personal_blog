package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.AdminViewModel

@Deprecated("默认管理端文章编辑 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun AdminPostEditScreen(
    viewModel: AdminViewModel,
    postId: Long?,
    onSaved: () -> Unit
) {
    DeprecatedUiScreen("AdminPostEditScreen")
}
