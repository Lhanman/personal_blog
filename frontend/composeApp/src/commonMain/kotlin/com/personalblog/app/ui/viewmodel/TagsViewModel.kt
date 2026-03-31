package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.repository.TagRepository
import com.personalblog.shared.dto.TagDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TagsState(
    val tags: List<TagDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TagsViewModel(
    private val tagRepository: TagRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TagsState())
    val state: StateFlow<TagsState> = _state.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val tags = tagRepository.getTags()
                _state.value = _state.value.copy(tags = tags, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
