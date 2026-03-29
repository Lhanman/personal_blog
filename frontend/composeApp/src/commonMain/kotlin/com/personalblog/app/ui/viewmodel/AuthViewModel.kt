package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.AuthRemoteDataSource
import com.personalblog.app.data.repository.TokenRepository
import com.personalblog.app.logging.LoggerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val role: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authDataSource: AuthRemoteDataSource,
    private val tokenRepository: TokenRepository
) : ViewModel() {
    private val logger = LoggerFactory.getLogger("AuthViewModel")
    private val _state = MutableStateFlow(
        AuthState(
            isLoggedIn = tokenRepository.isLoggedIn(),
            role = tokenRepository.getRole()
        )
    )
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            logger.info("attempting login", feature = "auth", extras = mapOf("email" to email))
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = authDataSource.login(email, password)
                tokenRepository.saveSession(response.token, response.user.role)
                logger.info("login succeeded", feature = "auth", extras = mapOf("role" to response.user.role))
                _state.value = _state.value.copy(
                    isLoggedIn = true,
                    role = response.user.role,
                    isLoading = false
                )
            } catch (e: Exception) {
                logger.error("login failed", feature = "auth", throwable = e, extras = mapOf("email" to email))
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            logger.info("attempting registration", feature = "auth", extras = mapOf("email" to email, "username" to username))
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = authDataSource.register(username, email, password)
                tokenRepository.saveSession(response.token, response.user.role)
                logger.info("registration succeeded", feature = "auth", extras = mapOf("role" to response.user.role))
                _state.value = _state.value.copy(
                    isLoggedIn = true,
                    role = response.user.role,
                    isLoading = false
                )
            } catch (e: Exception) {
                logger.error("registration failed", feature = "auth", throwable = e, extras = mapOf("email" to email))
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        logger.info("logging out", feature = "auth")
        tokenRepository.clearToken()
        _state.value = AuthState(isLoggedIn = false)
    }
}
