package com.personalblog.backend.routes

import com.personalblog.backend.repository.PostRepository
import com.personalblog.backend.repository.TagRepository
import com.personalblog.backend.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(
    postRepository: PostRepository,
    tagRepository: TagRepository,
    userRepository: UserRepository
) {
    authenticate("jwt-auth") {
        route("/api/v1/admin") {

            // --- Posts ---
            post("/posts") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.payload.getClaim("role").asString()
                if (role != "ADMIN") return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                val authorId = principal.payload.getClaim("userId").asLong()

                val body = call.receive<Map<String, Any?>>()
                val title = body["title"] as? String ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing title"))
                val slug = body["slug"] as? String ?: title.lowercase().replace(Regex("[^a-z0-9]+"), "-")
                val summary = body["summary"] as? String ?: ""
                val content = body["content"] as? String ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing content"))
                val coverImageUrl = body["coverImageUrl"] as? String
                @Suppress("UNCHECKED_CAST")
                val tagIds = (body["tagIds"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList()
                if (tagIds.size > 5) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Maximum 5 tags allowed"))

                call.respond(HttpStatusCode.Created, postRepository.create(title, slug, summary, content, coverImageUrl, tagIds, authorId))
            }

            put("/posts/{id}") {
                val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                if (role != "ADMIN") return@put call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                val body = call.receive<Map<String, Any?>>()
                val title = body["title"] as? String ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing title"))
                val slug = body["slug"] as? String ?: title.lowercase().replace(Regex("[^a-z0-9]+"), "-")
                val summary = body["summary"] as? String ?: ""
                val content = body["content"] as? String ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing content"))
                val coverImageUrl = body["coverImageUrl"] as? String
                @Suppress("UNCHECKED_CAST")
                val tagIds = (body["tagIds"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList()
                if (tagIds.size > 5) return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Maximum 5 tags allowed"))
                val published = body["published"] as? Boolean ?: false

                val updated = postRepository.update(id, title, slug, summary, content, coverImageUrl, tagIds, published)
                    ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
                call.respond(updated)
            }

            delete("/posts/{id}") {
                val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                if (role != "ADMIN") return@delete call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                if (postRepository.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            }

            // --- Users ---
            get("/users") {
                val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                if (role != "ADMIN") return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                call.respond(userRepository.findAll())
            }

            put("/users/{id}/role") {
                val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                if (role != "ADMIN") return@put call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                val body = call.receive<Map<String, String>>()
                val newRole = body["role"] ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing role"))
                if (userRepository.updateRole(id, newRole)) call.respond(mapOf("success" to true))
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }
}
