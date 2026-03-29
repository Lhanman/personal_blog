package com.personalblog.app.ui.viewmodel

import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
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
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class BlogListViewModelTest {
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
    fun `init loads first page of posts`() = runTest {
        val repository = FakePostRepository().apply {
            getPostsHandler = { _, _ ->
                PagedResponse(
                    items = listOf(samplePost(1), samplePost(2)),
                    total = 2,
                    page = 1,
                    size = 10,
                    totalPages = 1
                )
            }
        }

        val viewModel = BlogListViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.posts.size)
        assertEquals(listOf(1L, 2L), state.posts.map { it.id })
        assertFalse(state.hasMore)
        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun `loadPosts appends additional pages`() = runTest {
        val repository = FakePostRepository().apply {
            getPostsHandler = { page, _ ->
                when (page) {
                    1 -> PagedResponse(listOf(samplePost(1)), total = 2, page = 1, size = 10, totalPages = 2)
                    else -> PagedResponse(listOf(samplePost(2)), total = 2, page = 2, size = 10, totalPages = 2)
                }
            }
        }

        val viewModel = BlogListViewModel(repository)
        advanceUntilIdle()
        viewModel.loadPosts()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(1L, 2L), state.posts.map { it.id })
        assertFalse(state.hasMore)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    private fun samplePost(id: Long): PostDto = PostDto(
        id = id,
        title = "Post $id",
        slug = "post-$id",
        summary = "Summary $id",
        publishedAt = "2026-01-01",
        readingTimeMinutes = 3
    )
}
