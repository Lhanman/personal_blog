package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.TagRemoteDataSource
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TagPostsState(
    val slug: String,
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

class TagPostsViewModel(
    private val tagDataSource: TagRemoteDataSource,
    slug: String
) : ViewModel() {
    private val _state = MutableStateFlow(TagPostsState(slug = slug))
    val state: StateFlow<TagPostsState> = _state.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        if (_state.value.isLoading || !_state.value.hasMore) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = tagDataSource.getPostsByTag(_state.value.slug, _state.value.currentPage, 10)
                _state.value = _state.value.copy(
                    posts = _state.value.posts + response.items,
                    isLoading = false,
                    currentPage = _state.value.currentPage + 1,
                    hasMore = response.page < response.totalPages
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
