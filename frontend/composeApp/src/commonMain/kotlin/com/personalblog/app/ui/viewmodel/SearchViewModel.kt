package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.repository.PostRepository
import com.personalblog.app.logging.LoggerFactory
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val suggestions: List<PostDto> = emptyList(),
    val results: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val postDataSource: PostRepository
) : ViewModel() {
    private val logger = LoggerFactory.getLogger("SearchViewModel")
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        search(query)
                    } else {
                        _state.value = SearchState(query = query)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        logger.debug("search query changed", feature = "search", extras = mapOf("queryLength" to query.length.toString()))
        _state.value = _state.value.copy(query = query)
        _queryFlow.value = query
    }

    fun hideSuggestions() {
        _state.value = _state.value.copy(suggestions = emptyList())
    }

    private suspend fun search(query: String) {
        logger.info("searching posts", feature = "search", extras = mapOf("query" to query))
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val response = postDataSource.searchPosts(query, 1, 20)
            logger.info("search completed", feature = "search", extras = mapOf("resultCount" to response.items.size.toString()))
            _state.value = _state.value.copy(
                suggestions = if (query.length >= 2) response.items.take(5) else emptyList(),
                results = response.items,
                isLoading = false
            )
        } catch (e: Exception) {
            logger.error("search failed", feature = "search", throwable = e, extras = mapOf("query" to query))
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}
