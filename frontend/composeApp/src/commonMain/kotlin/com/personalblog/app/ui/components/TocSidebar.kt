package com.personalblog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TocItem(
    val title: String,
    val level: Int,
    val sectionIndex: Int
)

@Composable
fun TocSidebar(
    items: List<TocItem>,
    activeSectionIndex: Int?,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "目录",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        items.forEach { item ->
            val isActive = item.sectionIndex == activeSectionIndex
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item.sectionIndex) }
                    .padding(
                        start = ((item.level - 1) * 12).dp,
                        top = 4.dp,
                        bottom = 4.dp
                    )
            )
        }
    }
}
