package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.repository.PostRepository
import com.personalblog.app.logging.LoggerFactory
import com.personalblog.shared.dto.PostDto
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlogReaderState(
    val post: PostDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNotFound: Boolean = false
)

class BlogReaderViewModel(
    private val postDataSource: PostRepository,
    private val postId: Long
) : ViewModel() {
    private val logger = LoggerFactory.getLogger("BlogReaderViewModel")
    private val _state = MutableStateFlow(BlogReaderState())
    val state: StateFlow<BlogReaderState> = _state.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            logger.info("loading post", feature = "blog-reader", extras = mapOf("postId" to postId.toString()))
            _state.value = _state.value.copy(isLoading = true, error = null, isNotFound = false)
            try {
                val post = postDataSource.getPostById(postId)
                logger.info("post loaded", feature = "blog-reader", extras = mapOf("postId" to postId.toString()))
                _state.value = _state.value.copy(post = post, isLoading = false)
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    logger.warn("post not found", feature = "blog-reader", extras = mapOf("postId" to postId.toString()))
                    _state.value = _state.value.copy(isLoading = false, isNotFound = true, error = null)
                } else {
                    logger.error("failed to load post", feature = "blog-reader", throwable = e, extras = mapOf("postId" to postId.toString()))
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            } catch (e: Exception) {
                logger.error("failed to load post", feature = "blog-reader", throwable = e, extras = mapOf("postId" to postId.toString()))
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
