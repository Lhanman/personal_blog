package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.PostRemoteDataSource
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlogReaderState(
    val post: PostDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BlogReaderViewModel(
    private val postDataSource: PostRemoteDataSource,
    private val postId: Long
) : ViewModel() {
    private val _state = MutableStateFlow(BlogReaderState())
    val state: StateFlow<BlogReaderState> = _state.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val post = postDataSource.getPostById(postId)
                _state.value = _state.value.copy(post = post, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
