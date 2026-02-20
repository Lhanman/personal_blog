package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.personalblog.app.ui.DeprecatedUiComponent

data class TocItem(
    val title: String,
    val level: Int,
    val sectionIndex: Int
)

@Deprecated("默认 TOC 侧边栏 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun TocSidebar(
    items: List<TocItem>,
    activeSectionIndex: Int?,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    DeprecatedUiComponent("TocSidebar", modifier)
}
