package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.AuthRemoteDataSource
import com.personalblog.app.data.repository.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authDataSource: AuthRemoteDataSource,
    private val tokenRepository: TokenRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState(isLoggedIn = tokenRepository.isLoggedIn()))
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = authDataSource.login(email, password)
                tokenRepository.saveToken(response.token)
                _state.value = _state.value.copy(isLoggedIn = true, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = authDataSource.register(username, email, password)
                tokenRepository.saveToken(response.token)
                _state.value = _state.value.copy(isLoggedIn = true, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        tokenRepository.clearToken()
        _state.value = AuthState(isLoggedIn = false)
    }
}
