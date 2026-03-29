package com.personalblog.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommentInput(
    isLoggedIn: Boolean,
    isPosting: Boolean,
    onPostComment: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    if (!isLoggedIn) {
        Row {
            Text(
                text = "登录后可发表评论",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onLoginClick) { Text("去登录") }
        }
        return
    }

    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("写下你的评论...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { if (text.isNotBlank()) { onPostComment(text); text = "" } },
            enabled = !isPosting && text.isNotBlank()
        ) {
            Text(if (isPosting) "发送中..." else "发表评论")
        }
    }
}
