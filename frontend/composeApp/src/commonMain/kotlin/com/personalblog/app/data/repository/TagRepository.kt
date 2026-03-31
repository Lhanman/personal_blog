package com.personalblog.app.data.repository

import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto

interface TagRepository {
    suspend fun getTags(): List<TagDto>

    suspend fun getPostsByTag(slug: String, page: Int, size: Int): PagedResponse<PostDto>
}
