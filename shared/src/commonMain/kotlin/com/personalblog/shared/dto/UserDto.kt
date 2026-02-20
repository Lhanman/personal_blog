package com.personalblog.shared.dto
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val id: Long, val username: String, val email: String, val role: String)
