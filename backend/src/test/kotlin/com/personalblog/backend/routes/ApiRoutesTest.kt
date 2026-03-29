package com.personalblog.backend.routes

import com.personalblog.backend.TestDb
import com.personalblog.backend.module
import com.personalblog.backend.db.PostTagsTable
import com.personalblog.backend.db.PostsTable
import com.personalblog.backend.db.TagsTable
import com.personalblog.backend.repository.CommentRepository
import com.personalblog.backend.repository.PostRepository
import com.personalblog.backend.repository.TagRepository
import com.personalblog.backend.repository.UserRepository
import com.personalblog.shared.dto.AuthResponse
import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ApiRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setUp() {
        TestDb.connect()
        seedData()
    }

    @AfterTest
    fun tearDown() {
        TestDb.cleanup()
    }

    @Test
    fun `GET posts returns paged list`() = testApplication {
        application {
            module(
                initializeDatabase = false,
                postRepository = PostRepository(),
                tagRepository = TagRepository(),
                userRepository = UserRepository(),
                commentRepository = CommentRepository()
            )
        }

        val response = client.get("/api/v1/posts?page=1&size=10")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<PagedResponse<PostDto>>(response.bodyAsText())
        assertTrue(body.items.isNotEmpty())
        assertEquals(1, body.page)
    }

    @Test
    fun `GET tags returns tag counts`() = testApplication {
        application {
            module(
                initializeDatabase = false,
                postRepository = PostRepository(),
                tagRepository = TagRepository(),
                userRepository = UserRepository(),
                commentRepository = CommentRepository()
            )
        }

        val response = client.get("/api/v1/tags")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<List<TagDto>>(response.bodyAsText())
        assertTrue(body.isNotEmpty())
        assertEquals("kotlin", body.first().slug)
        assertEquals(1, body.first().postCount)
    }

    @Test
    fun `POST register returns JWT token`() = testApplication {
        application {
            module(
                initializeDatabase = false,
                postRepository = PostRepository(),
                tagRepository = TagRepository(),
                userRepository = UserRepository(),
                commentRepository = CommentRepository()
            )
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "username": "new-user",
                  "email": "new-user@example.com",
                  "password": "password123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = json.decodeFromString<AuthResponse>(response.bodyAsText())
        assertNotNull(body.token)
        assertEquals("USER", body.user.role)
    }

    private fun seedData() {
        val userId = TestDb.insertUser(username = "author", email = "author@example.com")
        val tagId = transaction {
            TagsTable.insert {
                it[TagsTable.name] = "Kotlin"
                it[TagsTable.slug] = "kotlin"
            }[TagsTable.id]
        }
        val postId = transaction {
            PostsTable.insert {
                it[PostsTable.title] = "Kotlin Post"
                it[PostsTable.slug] = "kotlin-post"
                it[PostsTable.summary] = "summary"
                it[PostsTable.content] = "content"
                it[PostsTable.coverImageUrl] = null
                it[PostsTable.published] = true
                it[PostsTable.authorId] = userId
                it[PostsTable.createdAt] = Clock.System.now()
                it[PostsTable.updatedAt] = Clock.System.now()
            }[PostsTable.id]
        }
        transaction {
            PostTagsTable.insert {
                it[PostTagsTable.postId] = postId
                it[PostTagsTable.tagId] = tagId
            }
        }
    }
}
