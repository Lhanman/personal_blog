package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.TagPostsViewModel

@Deprecated("默认标签文章列表 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun TagPostsScreen(
    viewModel: TagPostsViewModel,
    onPostClick: (Long) -> Unit
) {
    DeprecatedUiScreen("TagPostsScreen")
}
