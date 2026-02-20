package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiComponent
import com.personalblog.shared.dto.PostDto

@Deprecated("默认搜索结果卡片 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun SearchResultCard(post: PostDto, query: String, onClick: () -> Unit) {
    DeprecatedUiComponent("SearchResultCard")
}
