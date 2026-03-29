package com.personalblog.app.ui.viewmodel

import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
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
    fun `search uses 300ms debounce and latest query`() = runTest {
        val repository = FakePostRepository().apply {
            searchPostsHandler = { query, _, _ ->
                PagedResponse(
                    items = listOf(samplePost(1, query)),
                    total = 1,
                    page = 1,
                    size = 20,
                    totalPages = 1
                )
            }
        }

        val viewModel = SearchViewModel(repository)
        viewModel.onQueryChange("ko")
        advanceTimeBy(100)
        viewModel.onQueryChange("kotlin")

        advanceTimeBy(299)
        assertTrue(repository.searchQueries.isEmpty())

        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(listOf("kotlin"), repository.searchQueries)
        assertEquals("kotlin", viewModel.state.value.results.first().title.removePrefix("Result "))
    }

    @Test
    fun `suggestions are capped at five`() = runTest {
        val repository = FakePostRepository().apply {
            searchPostsHandler = { _, _, _ ->
                PagedResponse(
                    items = (1L..10L).map { samplePost(it, "kotlin") },
                    total = 10,
                    page = 1,
                    size = 20,
                    totalPages = 1
                )
            }
        }

        val viewModel = SearchViewModel(repository)
        viewModel.onQueryChange("kotlin")

        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.suggestions.size)
    }

    @Test
    fun `blank query does not trigger search`() = runTest {
        val repository = FakePostRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChange("")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertTrue(repository.searchQueries.isEmpty())
        assertTrue(viewModel.state.value.results.isEmpty())
    }

    private fun samplePost(id: Long, query: String): PostDto = PostDto(
        id = id,
        title = "Result $query",
        slug = "result-$id",
        summary = "Summary",
        publishedAt = "2026-01-01",
        readingTimeMinutes = 2
    )
}
