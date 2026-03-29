package com.personalblog.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.personalblog.app.ui.components.CommentInput
import com.personalblog.app.ui.components.CommentItem
import com.personalblog.app.ui.components.ContentContainer
import com.personalblog.app.ui.components.ContentWidthStyle
import com.personalblog.app.ui.components.TagChip
import com.personalblog.app.ui.components.TocItem
import com.personalblog.app.ui.components.TocSidebar
import com.personalblog.app.ui.viewmodel.BlogReaderViewModel
import com.personalblog.app.ui.viewmodel.CommentViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlogReaderScreen(
    viewModel: BlogReaderViewModel,
    commentViewModel: CommentViewModel,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val commentState by commentViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.isNotFound -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("文章不存在", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        }
        state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("加载失败：${state.error}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        state.post != null -> {
            val post = state.post!!
            val content = post.content ?: ""
            val tocItems = remember(content) {
                content.lines()
                    .mapIndexedNotNull { index, line ->
                        when {
                            line.startsWith("### ") -> TocItem(line.removePrefix("### "), 3, index)
                            line.startsWith("## ") -> TocItem(line.removePrefix("## "), 2, index)
                            line.startsWith("# ") -> TocItem(line.removePrefix("# "), 1, index)
                            else -> null
                        }
                    }
            }
            val activeSectionIndex by remember {
                derivedStateOf { (listState.firstVisibleItemIndex - 1).coerceAtLeast(0) }
            }

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val isDesktop = maxWidth >= 768.dp
                val showDesktopToc = isDesktop && tocItems.isNotEmpty() && maxWidth >= 1100.dp

                ContentContainer(
                    modifier = Modifier.fillMaxSize(),
                    widthStyle = if (showDesktopToc) ContentWidthStyle.Wide else ContentWidthStyle.Reading
                ) {
                    if (showDesktopToc) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                ArticleContent(
                                    post = post,
                                    content = content,
                                    commentState = commentState,
                                    isLoggedIn = isLoggedIn,
                                    listState = listState,
                                    onTagClick = onTagClick,
                                    onPostComment = commentViewModel::postComment,
                                    onLoginClick = onLoginClick
                                )
                            }

                            TocSidebar(
                                items = tocItems,
                                activeSectionIndex = activeSectionIndex,
                                onItemClick = { sectionIndex ->
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(sectionIndex + 1)
                                    }
                                },
                                modifier = Modifier
                                    .width(260.dp)
                                    .padding(top = 24.dp)
                            )
                        }
                    } else {
                        ArticleContent(
                            post = post,
                            content = content,
                            commentState = commentState,
                            isLoggedIn = isLoggedIn,
                            listState = listState,
                            onTagClick = onTagClick,
                            onPostComment = commentViewModel::postComment,
                            onLoginClick = onLoginClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArticleContent(
    post: com.personalblog.shared.dto.PostDto,
    content: String,
    commentState: com.personalblog.app.ui.viewmodel.CommentState,
    isLoggedIn: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onTagClick: (String) -> Unit,
    onPostComment: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.publishedAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "${post.readingTimeMinutes} min read",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (post.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.tags.forEach { tag ->
                        TagChip(tag = tag, onClick = { onTagClick(tag.slug) })
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))
        }

        items(content.lines()) { line ->
            MarkdownLine(line = line)
        }

        item {
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Text(
                text = "评论",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))
            CommentInput(
                isLoggedIn = isLoggedIn,
                isPosting = commentState.isPosting,
                onPostComment = onPostComment,
                onLoginClick = onLoginClick
            )
            Spacer(Modifier.height(16.dp))
        }

        items(commentState.comments) { comment ->
            CommentItem(comment = comment)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun MarkdownLine(line: String) {
    when {
        line.startsWith("# ") -> {
            Text(
                text = line.removePrefix("# "),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        line.startsWith("## ") -> {
            Text(
                text = line.removePrefix("## "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        line.startsWith("### ") -> {
            Text(
                text = line.removePrefix("### "),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        line.startsWith("- ") -> {
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = line.removePrefix("- "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        line.isBlank() -> Spacer(Modifier.height(12.dp))
        else -> {
            Text(
                text = line,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
