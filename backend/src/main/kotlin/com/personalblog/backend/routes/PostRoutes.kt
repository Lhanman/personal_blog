package com.personalblog.backend.routes

import com.personalblog.backend.repository.PostRepository
import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.postRoutes(postRepository: PostRepository) {
    route("/api/v1/posts") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            call.respond(postRepository.findAll(page, size))
        }

        get("/search") {
            val rawQuery = call.request.queryParameters["q"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, mapOf("error" to "Missing query parameter 'q'")
            )
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.takeIf { it > 0 } ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull()?.takeIf { it in 1..50 } ?: 10
            val query = rawQuery.trim()

            if (query.isBlank()) {
                return@get call.respond(PagedResponse<PostDto>(emptyList(), 0, page, size, 0))
            }

            call.respond(postRepository.search(query, page, size))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
            val post = postRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post not found"))
            call.respond(post)
        }
    }
}
