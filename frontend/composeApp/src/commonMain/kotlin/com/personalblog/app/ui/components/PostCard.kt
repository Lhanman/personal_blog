package com.personalblog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personalblog.shared.dto.PostDto
import org.jetbrains.compose.resources.painterResource
import personal_blog.composeapp.generated.resources.Res
import personal_blog.composeapp.generated.resources.ic_date

enum class PostCardVariant { Default, Compact, Featured }

@Composable
fun PostCard(
    post: PostDto,
    onClick: () -> Unit,
    variant: PostCardVariant = PostCardVariant.Default,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        // 标题
        Text(
            text = post.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFF6B01),
            maxLines = if (variant == PostCardVariant.Compact) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        // 日期行（图标 + 文字）
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_date),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = post.publishedAt.take(10),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 摘要（Compact 不显示）
        if (variant != PostCardVariant.Compact && post.summary.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = post.summary,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (variant == PostCardVariant.Featured) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
