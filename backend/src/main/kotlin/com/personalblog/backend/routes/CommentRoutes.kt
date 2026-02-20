package com.personalblog.backend.routes

import com.personalblog.backend.repository.CommentRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.commentRoutes(commentRepository: CommentRepository) {
    route("/api/v1/comments") {
        get("/{postId}") {
            val postId = call.parameters["postId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid postId"))
            call.respond(commentRepository.findByPostId(postId))
        }

        authenticate("jwt-auth") {
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asLong()
                val body = call.receive<Map<String, String>>()
                val postId = body["postId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing postId"))
                val content = body["content"]?.takeIf { it.isNotBlank() }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing content"))
                call.respond(HttpStatusCode.Created, commentRepository.create(postId, userId, content))
            }
        }
    }
}
