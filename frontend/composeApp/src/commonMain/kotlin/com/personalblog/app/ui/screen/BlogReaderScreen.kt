package com.personalblog.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalblog.app.ui.components.CommentInput
import com.personalblog.app.ui.components.CommentItem
import com.personalblog.app.ui.viewmodel.BlogReaderViewModel
import com.personalblog.app.ui.viewmodel.CommentViewModel

@Composable
fun BlogReaderScreen(
    viewModel: BlogReaderViewModel,
    commentViewModel: CommentViewModel,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val commentState by commentViewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}")
            }
        }
        state.post != null -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = state.post!!.title,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.post!!.publishedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.post!!.summary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(commentState.comments) { comment ->
                    CommentItem(comment = comment)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    CommentInput(
                        isLoggedIn = isLoggedIn,
                        isPosting = commentState.isPosting,
                        onPostComment = { commentViewModel.postComment(it) },
                        onLoginClick = onLoginClick
                    )
                }
            }
        }
    }
}
