package com.personalblog.backend.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.personalblog.backend.auth.JwtConfig
import com.personalblog.backend.repository.UserRepository
import com.personalblog.shared.dto.AuthRequest
import com.personalblog.shared.dto.AuthResponse
import com.personalblog.shared.dto.RegisterRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api/v1/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (userRepository.findByEmail(req.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already registered"))
            }
            val hash = BCrypt.withDefaults().hashToString(10, req.password.toCharArray())
            val user = userRepository.create(req.username, req.email, hash)
            val token = JwtConfig.generateToken(user.id, user.role)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user))
        }

        post("/login") {
            val req = call.receive<AuthRequest>()
            val record = userRepository.findByEmail(req.email)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            val valid = BCrypt.verifyer().verify(req.password.toCharArray(), record.passwordHash).verified
            if (!valid) return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            val token = JwtConfig.generateToken(record.id, record.role)
            val userDto = com.personalblog.shared.dto.UserDto(record.id, record.username, record.email, record.role)
            call.respond(AuthResponse(token, userDto))
        }
    }
}
