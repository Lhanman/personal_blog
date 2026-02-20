package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.CommentRemoteDataSource
import com.personalblog.shared.dto.CommentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommentState(
    val comments: List<CommentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPosting: Boolean = false
)

class CommentViewModel(
    private val commentDataSource: CommentRemoteDataSource,
    private val postId: Long
) : ViewModel() {
    private val _state = MutableStateFlow(CommentState())
    val state: StateFlow<CommentState> = _state.asStateFlow()

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val comments = commentDataSource.getComments(postId)
                _state.value = _state.value.copy(comments = comments, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun postComment(content: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPosting = true, error = null)
            try {
                val newComment = commentDataSource.postComment(postId, content)
                _state.value = _state.value.copy(
                    comments = _state.value.comments + newComment,
                    isPosting = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isPosting = false, error = e.message)
            }
        }
    }
}
