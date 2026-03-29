package com.personalblog.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val title: String,
    val slug: String? = null,
    val summary: String? = null,
    val content: String,
    val coverImageUrl: String? = null,
    val tagIds: List<Long> = emptyList(),
    val published: Boolean = false
)

@Serializable
data class UpdatePostRequest(
    val title: String,
    val slug: String? = null,
    val summary: String? = null,
    val content: String,
    val coverImageUrl: String? = null,
    val tagIds: List<Long> = emptyList(),
    val published: Boolean = false
)

@Serializable
data class UpdateUserRoleRequest(
    val role: String
)
