package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.repository.PostRepository
import com.personalblog.app.logging.LoggerFactory
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlogListState(
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

class BlogListViewModel(
    private val postDataSource: PostRepository
) : ViewModel() {
    private val logger = LoggerFactory.getLogger("BlogListViewModel")
    private val _state = MutableStateFlow(BlogListState())
    val state: StateFlow<BlogListState> = _state.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            logger.info("loading posts", feature = "blog-list", extras = mapOf("page" to _state.value.currentPage.toString()))
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = postDataSource.getPosts(_state.value.currentPage, 10)
                logger.info(
                    "posts loaded",
                    feature = "blog-list",
                    extras = mapOf("count" to response.items.size.toString(), "page" to response.page.toString())
                )
                _state.value = _state.value.copy(
                    posts = _state.value.posts + response.items,
                    isLoading = false,
                    currentPage = _state.value.currentPage + 1,
                    hasMore = response.page < response.totalPages
                )
            } catch (e: Exception) {
                logger.error("failed to load posts", feature = "blog-list", throwable = e)
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun refresh() {
        _state.value = BlogListState()
        loadPosts()
    }
}
