package com.personalblog.app.data.remote

import com.personalblog.shared.dto.*

class TagRemoteDataSource(private val client: ApiClient) {
    suspend fun getTags(): List<TagDto> = client.get("/api/v1/tags")

    suspend fun getPostsByTag(slug: String, page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/tags/$slug", mapOf("page" to page.toString(), "size" to size.toString()))
}
