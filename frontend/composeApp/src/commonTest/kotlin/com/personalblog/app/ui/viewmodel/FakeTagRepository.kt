package com.personalblog.app.ui.viewmodel

import com.personalblog.app.data.repository.TagRepository
import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto

class FakeTagRepository : TagRepository {
    val requestedSlugs = mutableListOf<String>()

    var getTagsHandler: suspend () -> List<TagDto> = { emptyList() }
    var getPostsByTagHandler: suspend (slug: String, page: Int, size: Int) -> PagedResponse<PostDto> = { _, _, _ ->
        PagedResponse(emptyList(), 0, 1, 10, 0)
    }

    override suspend fun getTags(): List<TagDto> = getTagsHandler()

    override suspend fun getPostsByTag(slug: String, page: Int, size: Int): PagedResponse<PostDto> {
        requestedSlugs += slug
        return getPostsByTagHandler(slug, page, size)
    }
}
