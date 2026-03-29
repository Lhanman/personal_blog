package com.personalblog.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalblog.app.ui.components.ContentContainer
import com.personalblog.app.ui.components.ContentWidthStyle
import com.personalblog.app.ui.components.EmptyState
import com.personalblog.app.ui.components.PostCard
import com.personalblog.app.ui.components.PostCardVariant
import com.personalblog.app.ui.components.SectionTitle
import com.personalblog.app.ui.viewmodel.TagPostsViewModel

@Composable
fun TagPostsScreen(
    viewModel: TagPostsViewModel,
    onPostClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()

    ContentContainer(widthStyle = ContentWidthStyle.Page) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Spacer(Modifier.height(24.dp))
                SectionTitle(title = "Tag: ${state.slug}")
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading && state.posts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.posts.isEmpty() && !state.isLoading) {
                item { EmptyState(message = "该标签下暂无文章") }
            } else {
                items(state.posts) { post ->
                    PostCard(
                        post = post,
                        variant = PostCardVariant.Default,
                        onClick = { onPostClick(post.id) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                if (state.hasMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(onClick = { viewModel.loadPosts() }) {
                                    Text("加载更多")
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
