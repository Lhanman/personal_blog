package com.personalblog.app.data.repository

import com.russhwolf.settings.Settings

class TokenRepository(private val settings: Settings) {
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ROLE = "user_role"
    }

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)

    fun saveToken(token: String) = settings.putString(KEY_TOKEN, token)

    fun getRole(): String? = settings.getStringOrNull(KEY_ROLE)

    fun saveRole(role: String) = settings.putString(KEY_ROLE, role)

    fun saveSession(token: String, role: String) {
        saveToken(token)
        saveRole(role)
    }

    fun clearToken() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_ROLE)
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
