package com.personalblog.shared.dto
import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(val items: List<T>, val total: Long, val page: Int, val size: Int, val totalPages: Int)
