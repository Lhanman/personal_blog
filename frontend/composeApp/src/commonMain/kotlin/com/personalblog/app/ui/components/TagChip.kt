package com.personalblog.app.ui.components

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiComponent
import com.personalblog.shared.dto.TagDto

@Deprecated("默认标签芯片 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun TagChip(tag: TagDto, onClick: () -> Unit) {
    DeprecatedUiComponent("TagChip")
}
