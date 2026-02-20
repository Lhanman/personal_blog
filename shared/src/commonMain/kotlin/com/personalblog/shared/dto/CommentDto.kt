package com.personalblog.shared.dto
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(val id: Long, val postId: Long, val authorName: String, val content: String, val createdAt: String)
