package com.personalblog.backend.routes

import com.personalblog.backend.repository.PostRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(postRepository: PostRepository) {
    route("/api/v1/posts") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            call.respond(postRepository.findAll(page, size))
        }

        get("/search") {
            val q = call.request.queryParameters["q"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, mapOf("error" to "Missing query parameter 'q'")
            )
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            // 根据环境选择搜索方法
            // 生产环境使用 PostgreSQL 全文搜索，开发/测试环境使用 ILIKE
            val useFullTextSearch = System.getenv("USE_FULLTEXT_SEARCH")?.toBoolean() ?: false
            val result = if (useFullTextSearch) {
                postRepository.searchWithFullText(q, page, size)
            } else {
                postRepository.search(q, page, size)
            }
            call.respond(result)
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
