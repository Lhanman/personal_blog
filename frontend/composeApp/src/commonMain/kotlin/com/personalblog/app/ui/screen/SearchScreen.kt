package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.SearchViewModel

@Deprecated("默认搜索 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun SearchScreen(viewModel: SearchViewModel, onPostClick: (Long) -> Unit) {
    DeprecatedUiScreen("SearchScreen")
}
