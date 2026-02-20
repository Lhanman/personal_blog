package com.personalblog.shared.dto
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(val id: Long, val name: String, val slug: String, val postCount: Int = 0)
