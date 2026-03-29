package com.personalblog.app.data.remote

import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AdminPostPayload(
    val title: String,
    val slug: String,
    val summary: String,
    val content: String,
    val coverImageUrl: String? = null,
    val tagIds: List<Long> = emptyList(),
    val published: Boolean = false
)

@Serializable
data class UpdateUserRolePayload(val role: String)

@Serializable
private data class AdminActionResponse(val success: Boolean)

class AdminRemoteDataSource(private val client: ApiClient) {
    suspend fun createPost(payload: AdminPostPayload): PostDto =
        client.post("/api/v1/admin/posts", payload)

    suspend fun updatePost(id: Long, payload: AdminPostPayload): PostDto =
        client.put("/api/v1/admin/posts/$id", payload)

    suspend fun deletePost(id: Long) {
        client.delete("/api/v1/admin/posts/$id")
    }

    suspend fun getUsers(): List<UserDto> = client.get("/api/v1/admin/users")

    suspend fun updateUserRole(userId: Long, role: String): Boolean {
        val response: AdminActionResponse = client.put("/api/v1/admin/users/$userId/role", UpdateUserRolePayload(role))
        return response.success
    }
}
