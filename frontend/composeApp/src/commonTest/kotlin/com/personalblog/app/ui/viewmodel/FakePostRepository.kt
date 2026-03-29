package com.personalblog.app.ui.viewmodel

import com.personalblog.app.data.repository.PostRepository
import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto

class FakePostRepository : PostRepository {
    val requestedPages = mutableListOf<Int>()
    val searchQueries = mutableListOf<String>()

    var getPostsHandler: suspend (page: Int, size: Int) -> PagedResponse<PostDto> = { _, _ ->
        PagedResponse(emptyList(), 0, 1, 10, 0)
    }
    var getPostByIdHandler: suspend (id: Long) -> PostDto = {
        throw IllegalStateException("No post configured for id=$it")
    }
    var searchPostsHandler: suspend (query: String, page: Int, size: Int) -> PagedResponse<PostDto> = { _, _, _ ->
        PagedResponse(emptyList(), 0, 1, 10, 0)
    }

    override suspend fun getPosts(page: Int, size: Int): PagedResponse<PostDto> {
        requestedPages += page
        return getPostsHandler(page, size)
    }

    override suspend fun getPostById(id: Long): PostDto {
        return getPostByIdHandler(id)
    }

    override suspend fun searchPosts(query: String, page: Int, size: Int): PagedResponse<PostDto> {
        searchQueries += query
        return searchPostsHandler(query, page, size)
    }
}
