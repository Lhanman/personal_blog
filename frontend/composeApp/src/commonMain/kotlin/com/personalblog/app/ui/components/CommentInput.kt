package com.personalblog.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommentInput(
    isLoggedIn: Boolean,
    isPosting: Boolean,
    onPostComment: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var content by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isLoggedIn) {
            TextButton(onClick = onLoginClick) {
                Text("Login to comment")
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Write a comment...") },
                minLines = 3,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onPostComment(content)
                        content = ""
                    }
                },
                enabled = !isPosting && content.isNotBlank()
            ) {
                if (isPosting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Post Comment")
                }
            }
        }
    }
}
