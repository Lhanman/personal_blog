package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.PostRemoteDataSource
import com.personalblog.shared.dto.PostDto
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val postDataSource: PostRemoteDataSource
) : ViewModel() {
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
        _state.value = _state.value.copy(query = query)
        _queryFlow.value = query
    }

    private suspend fun search(query: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val response = postDataSource.searchPosts(query, 0, 20)
            _state.value = _state.value.copy(
                results = response.items,
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}
