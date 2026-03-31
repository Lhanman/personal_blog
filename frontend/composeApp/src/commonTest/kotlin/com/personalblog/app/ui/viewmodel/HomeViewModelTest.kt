package com.personalblog.app.ui.viewmodel

import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads featured posts and three recent posts`() = runTest {
        val postRepository = FakePostRepository().apply {
            getPostsHandler = { _, _ ->
                PagedResponse(
                    items = listOf(
                        samplePost(101, featured = true),
                        samplePost(11),
                        samplePost(12),
                        samplePost(13)
                    ),
                    total = 4,
                    page = 1,
                    size = 3,
                    totalPages = 1
                )
            }
        }
        val tagRepository = FakeTagRepository().apply {
            getPostsByTagHandler = { _, _, _ ->
                PagedResponse(
                    items = listOf(
                        samplePost(101, featured = true),
                        samplePost(102, featured = true),
                        samplePost(103, featured = true)
                    ),
                    total = 3,
                    page = 1,
                    size = 3,
                    totalPages = 1
                )
            }
        }

        val viewModel = HomeViewModel(postRepository, tagRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(101L, 102L, 103L), state.featuredPosts.map { it.id })
        assertEquals(listOf(101L, 11L, 12L), state.recentPosts.map { it.id })
        assertEquals(listOf("featured"), tagRepository.requestedSlugs)
        assertEquals(listOf(1), postRepository.requestedPages)
    }

    @Test
    fun `recent posts use latest three posts regardless of featured tag`() = runTest {
        val postRepository = FakePostRepository().apply {
            getPostsHandler = { _, _ ->
                PagedResponse(
                    items = listOf(
                        samplePost(101, featured = true),
                        samplePost(102, featured = true),
                        samplePost(11),
                        samplePost(12)
                    ),
                    total = 4,
                    page = 1,
                    size = 3,
                    totalPages = 2
                )
            }
        }
        val tagRepository = FakeTagRepository().apply {
            getPostsByTagHandler = { _, _, _ ->
                PagedResponse(
                    items = listOf(samplePost(101, featured = true), samplePost(102, featured = true)),
                    total = 2,
                    page = 1,
                    size = 3,
                    totalPages = 1
                )
            }
        }

        val viewModel = HomeViewModel(postRepository, tagRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(101L, 102L, 11L), state.recentPosts.map { it.id })
        assertEquals(listOf(1), postRepository.requestedPages)
    }

    private fun samplePost(id: Long, featured: Boolean = false): PostDto = PostDto(
        id = id,
        title = "Post $id",
        slug = "post-$id",
        summary = "Summary $id",
        publishedAt = "2026-01-01",
        tags = if (featured) listOf(TagDto(id = 1, name = "Featured", slug = "featured")) else emptyList(),
        readingTimeMinutes = 3
    )
}
