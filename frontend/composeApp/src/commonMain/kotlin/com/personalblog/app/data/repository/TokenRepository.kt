package com.personalblog.app.data.repository

import com.russhwolf.settings.Settings

class TokenRepository(private val settings: Settings) {
    companion object {
        private const val KEY_TOKEN = "jwt_token"
    }

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)

    fun saveToken(token: String) = settings.putString(KEY_TOKEN, token)

    fun clearToken() = settings.remove(KEY_TOKEN)

    fun isLoggedIn(): Boolean = getToken() != null
}
