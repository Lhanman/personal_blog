package com.personalblog.app.data.remote

import com.personalblog.shared.dto.*

class AuthRemoteDataSource(private val client: ApiClient) {
    suspend fun login(email: String, password: String): AuthResponse =
        client.post("/api/v1/auth/login", AuthRequest(email, password))

    suspend fun register(username: String, email: String, password: String): AuthResponse =
        client.post("/api/v1/auth/register", RegisterRequest(username, email, password))
}
