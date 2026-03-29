package com.personalblog.app.data.repository

import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto

interface PostRepository {
    suspend fun getPosts(page: Int, size: Int): PagedResponse<PostDto>

    suspend fun getPostById(id: Long): PostDto

    suspend fun searchPosts(query: String, page: Int, size: Int): PagedResponse<PostDto>
}
