package com.personalblog.app.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import personal_blog.composeapp.generated.resources.Res
import personal_blog.composeapp.generated.resources.ic_rss

@Composable
fun HeroSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LhanBoyy",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (48 * 0.03).sp
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                painter = painterResource(Res.drawable.ic_rss),
                contentDescription = "RSS",
                tint = Color.Unspecified,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Halo ！ 我是 LhanBoyy，广州人，广东工业大学毕业，转专业入坑程序猿。" +
                   "热爱折腾技术，这个博客基于 KMP 编写，跨平台运行于 Android、iOS、Desktop 和 Web，" +
                   "用来记录技术探索和生活碎片。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
