package com.personalblog.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personalblog.app.ui.components.BrandDivider
import com.personalblog.app.ui.components.ContentContainer
import com.personalblog.app.ui.components.ContentWidthStyle
import com.personalblog.app.ui.components.EmptyState
import com.personalblog.app.ui.components.Footer
import com.personalblog.app.ui.components.HeroSection
import com.personalblog.app.ui.components.PostCard
import com.personalblog.app.ui.components.PostCardVariant
import com.personalblog.app.ui.components.SectionTitle
import com.personalblog.app.ui.viewmodel.BlogListViewModel

@Composable
fun BlogListScreen(
    viewModel: BlogListViewModel,
    onPostClick: (Long) -> Unit,
    onAllPostsClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val featuredPosts = state.posts.filter { it.tags.any { t -> t.slug == "featured" } }.take(3)
    val recentPosts = state.posts.filter { it.tags.none { t -> t.slug == "featured" } }.take(3)

    ContentContainer(widthStyle = ContentWidthStyle.Wide) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                HeroSection(modifier = Modifier.padding(top = 16.dp))
                Spacer(Modifier.height(16.dp))
                BrandDivider()
                Spacer(Modifier.height(24.dp))
            }

            if (featuredPosts.isNotEmpty()) {
                item {
                    SectionTitle(title = "Featured")
                    Spacer(Modifier.height(8.dp))
                }
                items(featuredPosts) { post ->
                    PostCard(
                        post = post,
                        variant = PostCardVariant.Featured,
                        onClick = { onPostClick(post.id) }
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    BrandDivider()
                    Spacer(Modifier.height(24.dp))
                }
            }

            item {
                SectionTitle(title = "Recent Posts")
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading && state.posts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (recentPosts.isEmpty() && !state.isLoading) {
                item { EmptyState(message = "暂无文章") }
            } else {
                items(recentPosts) { post ->
                    PostCard(
                        post = post,
                        variant = PostCardVariant.Default,
                        onClick = { onPostClick(post.id) }
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onAllPostsClick) {
                            Text("All Posts →", color = Color(0xFFFF6B01))
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                BrandDivider()
                Footer()
            }
        }
    }
}
