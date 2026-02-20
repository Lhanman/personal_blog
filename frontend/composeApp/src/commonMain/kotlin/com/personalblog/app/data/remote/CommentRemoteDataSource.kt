package com.personalblog.app.data.remote

import com.personalblog.shared.dto.CommentDto

class CommentRemoteDataSource(private val client: ApiClient) {
    suspend fun getComments(postId: Long): List<CommentDto> =
        client.get("/api/v1/comments/$postId")

    suspend fun postComment(postId: Long, content: String): CommentDto =
        client.post("/api/v1/comments", mapOf("postId" to postId.toString(), "content" to content))
}
