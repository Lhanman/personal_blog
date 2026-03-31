package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.repository.PostRepository
import com.personalblog.app.data.repository.TagRepository
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val FEATURED_TAG_SLUG = "featured"
private const val HOME_SECTION_SIZE = 3

data class HomeState(
    val featuredPosts: List<PostDto> = emptyList(),
    val recentPosts: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val postRepository: PostRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val featuredPosts = tagRepository
                    .getPostsByTag(FEATURED_TAG_SLUG, page = 1, size = HOME_SECTION_SIZE)
                    .items
                    .take(HOME_SECTION_SIZE)

                val recentPosts = postRepository
                    .getPosts(page = 1, size = HOME_SECTION_SIZE)
                    .items
                    .take(HOME_SECTION_SIZE)

                _state.value = HomeState(
                    featuredPosts = featuredPosts,
                    recentPosts = recentPosts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun refresh() {
        _state.value = HomeState()
        loadHome()
    }
}
