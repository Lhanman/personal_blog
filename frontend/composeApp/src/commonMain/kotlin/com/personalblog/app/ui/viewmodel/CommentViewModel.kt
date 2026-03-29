package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.CommentRemoteDataSource
import com.personalblog.app.logging.LoggerFactory
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
    private val logger = LoggerFactory.getLogger("CommentViewModel")
    private val _state = MutableStateFlow(CommentState())
    val state: StateFlow<CommentState> = _state.asStateFlow()

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            logger.info("loading comments", feature = "comments", extras = mapOf("postId" to postId.toString()))
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val comments = commentDataSource.getComments(postId)
                logger.info("comments loaded", feature = "comments", extras = mapOf("count" to comments.size.toString(), "postId" to postId.toString()))
                _state.value = _state.value.copy(comments = comments, isLoading = false)
            } catch (e: Exception) {
                logger.error("failed to load comments", feature = "comments", throwable = e, extras = mapOf("postId" to postId.toString()))
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun postComment(content: String) {
        viewModelScope.launch {
            logger.info("posting comment", feature = "comments", extras = mapOf("postId" to postId.toString(), "contentLength" to content.length.toString()))
            _state.value = _state.value.copy(isPosting = true, error = null)
            try {
                val newComment = commentDataSource.postComment(postId, content)
                logger.info("comment posted", feature = "comments", extras = mapOf("postId" to postId.toString()))
                _state.value = _state.value.copy(
                    comments = _state.value.comments + newComment,
                    isPosting = false
                )
            } catch (e: Exception) {
                logger.error("failed to post comment", feature = "comments", throwable = e, extras = mapOf("postId" to postId.toString()))
                _state.value = _state.value.copy(isPosting = false, error = e.message)
            }
        }
    }
}
