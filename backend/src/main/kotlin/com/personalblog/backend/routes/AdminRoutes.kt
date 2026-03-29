package com.personalblog.backend.routes

import com.personalblog.backend.repository.CommentRepository
import com.personalblog.backend.repository.PostRepository
import com.personalblog.backend.repository.TagRepository
import com.personalblog.backend.repository.UserRepository
import com.personalblog.shared.dto.CreatePostRequest
import com.personalblog.shared.dto.UpdatePostRequest
import com.personalblog.shared.dto.UpdateUserRoleRequest
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
    userRepository: UserRepository,
    commentRepository: CommentRepository
) {
    authenticate("jwt-auth") {
        route("/api/v1/admin") {

            // 统一角色校验：所有 admin 路由都要求 ADMIN 角色
            intercept(io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Call) {
                val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                    finish()
                }
            }

            // --- Posts ---
            post("/posts") {
                val principal = call.principal<JWTPrincipal>()!!
                val authorId = principal.payload.getClaim("userId").asLong()

                val req = call.receive<CreatePostRequest>()
                val slug = req.slug ?: req.title.lowercase().replace(Regex("[^a-z0-9]+"), "-")
                val summary = req.summary ?: ""
                if (req.tagIds.size > 5) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Maximum 5 tags allowed"))

                call.respond(HttpStatusCode.Created, postRepository.create(req.title, slug, summary, req.content, req.coverImageUrl, req.tagIds, authorId, req.published))
            }

            put("/posts/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                val req = call.receive<UpdatePostRequest>()
                val slug = req.slug ?: req.title.lowercase().replace(Regex("[^a-z0-9]+"), "-")
                val summary = req.summary ?: ""
                if (req.tagIds.size > 5) return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Maximum 5 tags allowed"))

                val updated = postRepository.update(id, req.title, slug, summary, req.content, req.coverImageUrl, req.tagIds, req.published)
                    ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
                call.respond(updated)
            }

            delete("/posts/{id}") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                if (postRepository.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            }

            // --- Users ---
            get("/users") {
                call.respond(userRepository.findAll())
            }

            put("/users/{id}/role") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                val req = call.receive<UpdateUserRoleRequest>()
                if (userRepository.updateRole(id, req.role)) call.respond(mapOf("success" to true))
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }

            delete("/users/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                if (userRepository.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }

            delete("/comments/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                if (commentRepository.hardDelete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Comment not found"))
            }
        }
    }
}
