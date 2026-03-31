package com.personalblog.app.data.remote

import com.personalblog.app.data.repository.TagRepository
import com.personalblog.shared.dto.*

class TagRemoteDataSource(private val client: ApiClient) : TagRepository {
    override suspend fun getTags(): List<TagDto> = client.get("/api/v1/tags")

    override suspend fun getPostsByTag(slug: String, page: Int, size: Int): PagedResponse<PostDto> =
        client.get("/api/v1/tags/$slug", mapOf("page" to page.toString(), "size" to size.toString()))
}
