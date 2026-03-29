package com.personalblog.app.data.remote

import com.personalblog.app.data.repository.PostRepository
import com.personalblog.shared.dto.*

class PostRemoteDataSource(private val client: ApiClient) : PostRepository {
    override suspend fun getPosts(page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/posts", mapOf("page" to page.toString(), "size" to size.toString()))

    override suspend fun getPostById(id: Long): PostDto =
        client.get("/api/v1/posts/$id")

    override suspend fun searchPosts(query: String, page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/posts/search", mapOf("q" to query, "page" to page.toString(), "size" to size.toString()))
}
