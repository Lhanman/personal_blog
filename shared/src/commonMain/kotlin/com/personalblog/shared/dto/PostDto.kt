package com.personalblog.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val title: String,
    val slug: String,
    val summary: String,
    val content: String? = null,
    val coverImageUrl: String? = null,
    val publishedAt: String,
    val updatedAt: String? = null,
    val tags: List<TagDto> = emptyList(),
    val readingTimeMinutes: Int,
    val published: Boolean = true,
    val authorName: String? = null
)
