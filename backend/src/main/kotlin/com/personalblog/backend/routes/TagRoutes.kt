package com.personalblog.backend.routes

import com.personalblog.backend.repository.TagRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tagRoutes(tagRepository: TagRepository) {
    route("/api/v1/tags") {
        get {
            call.respond(tagRepository.findAll())
        }

        get("/{slug}") {
            val slug = call.parameters["slug"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing slug"))
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            call.respond(tagRepository.findPostsByTagSlug(slug, page, size))
        }
    }
}
