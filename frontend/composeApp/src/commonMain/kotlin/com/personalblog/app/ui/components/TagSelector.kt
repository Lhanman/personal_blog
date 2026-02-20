package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.personalblog.app.ui.DeprecatedUiComponent
import com.personalblog.shared.dto.TagDto

@Deprecated("默认标签选择器 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun TagSelector(
    tags: List<TagDto>,
    selectedTagIds: Set<Long>,
    onToggleTag: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    DeprecatedUiComponent("TagSelector", modifier)
}
