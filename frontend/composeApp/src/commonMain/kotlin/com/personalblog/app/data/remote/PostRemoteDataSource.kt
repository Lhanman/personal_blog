package com.personalblog.app.data.remote

import com.personalblog.shared.dto.*

class PostRemoteDataSource(private val client: ApiClient) {
    suspend fun getPosts(page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/posts", mapOf("page" to page.toString(), "size" to size.toString()))

    suspend fun getPostById(id: Long): PostDto =
        client.get("/api/v1/posts/$id")

    suspend fun searchPosts(query: String, page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/posts/search", mapOf("q" to query, "page" to page.toString(), "size" to size.toString()))
}
