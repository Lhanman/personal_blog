package com.personalblog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalblog.shared.dto.TagDto

@Composable
fun TagChip(tag: TagDto, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text("${tag.name} (${tag.postCount})") },
        modifier = Modifier.padding(4.dp)
    )
}
