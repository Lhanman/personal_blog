package com.personalblog.backend

import com.personalblog.backend.db.DatabaseFactory
import com.personalblog.backend.plugins.configureAuth
import com.personalblog.backend.plugins.configurePlugins
import com.personalblog.backend.repository.*
import com.personalblog.backend.routes.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module(
    initializeDatabase: Boolean = true,
    postRepository: PostRepository = PostRepository(),
    tagRepository: TagRepository = TagRepository(),
    userRepository: UserRepository = UserRepository(),
    commentRepository: CommentRepository = CommentRepository()
) {
    if (initializeDatabase) {
        val dbHost = System.getenv("DB_HOST") ?: "localhost"
        val dbPort = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "personalblog"
        val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://$dbHost:$dbPort/$dbName"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"
        DatabaseFactory.init(dbUrl, dbUser, dbPassword)
    }

    configurePlugins()
    configureAuth()

    routing {
        postRoutes(postRepository)
        tagRoutes(tagRepository)
        authRoutes(userRepository)
        commentRoutes(commentRepository)
        adminRoutes(postRepository, tagRepository, userRepository, commentRepository)
    }
}
