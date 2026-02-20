package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.TagsViewModel

@Deprecated("默认标签墙 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun TagsWallScreen(viewModel: TagsViewModel, onTagClick: (String) -> Unit) {
    DeprecatedUiScreen("TagsWallScreen")
}
