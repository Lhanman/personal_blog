package com.personalblog.backend.repository

import com.personalblog.backend.TestDb
import com.personalblog.backend.db.PostTagsTable
import com.personalblog.backend.db.PostsTable
import com.personalblog.backend.db.TagsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class PostRepositoryTest {
    private lateinit var repository: PostRepository

    @BeforeTest
    fun setUp() {
        TestDb.connect()
        repository = PostRepository()
    }

    @AfterTest
    fun tearDown() {
        TestDb.cleanup()
    }

    @Test
    fun `findAll returns only published posts with pagination`() {
        val authorId = TestDb.insertUser()
        val now = Clock.System.now()

        val publishedOlder = insertPost(
            title = "Older",
            slug = "older",
            summary = "old",
            content = "old content",
            published = true,
            authorId = authorId,
            createdAt = now.minus(1, DateTimeUnit.HOUR, TimeZone.UTC)
        )
        val publishedNewer = insertPost(
            title = "Newer",
            slug = "newer",
            summary = "new",
            content = "new content",
            published = true,
            authorId = authorId,
            createdAt = now
        )
        insertPost(
            title = "Draft",
            slug = "draft",
            summary = "draft",
            content = "draft content",
            published = false,
            authorId = authorId,
            createdAt = now
        )

        val response = repository.findAll(page = 1, size = 10)

        assertEquals(2, response.items.size)
        assertEquals(listOf(publishedNewer, publishedOlder), response.items.map { it.id })
        assertTrue(response.items.all { it.content == null })
        assertEquals(2L, response.total)
    }

    @Test
    fun `findById returns post with tags`() {
        val authorId = TestDb.insertUser()
        val kotlinTagId = insertTag("Kotlin", "kotlin")
        val composeTagId = insertTag("Compose", "compose")

        val postId = insertPost(
            title = "Tagged",
            slug = "tagged",
            summary = "summary",
            content = "content",
            published = true,
            authorId = authorId,
            createdAt = Clock.System.now()
        )

        transaction {
            PostTagsTable.insert {
                it[PostTagsTable.postId] = postId
                it[PostTagsTable.tagId] = kotlinTagId
            }
            PostTagsTable.insert {
                it[PostTagsTable.postId] = postId
                it[PostTagsTable.tagId] = composeTagId
            }
        }

        val post = repository.findById(postId)

        assertNotNull(post)
        assertEquals(postId, post.id)
        assertEquals(2, post.tags.size)
        assertEquals(setOf("kotlin", "compose"), post.tags.map { it.slug }.toSet())
    }

    @Test
    fun `search matches title and summary`() {
        val authorId = TestDb.insertUser()
        insertPost(
            title = "Kotlin Coroutines",
            slug = "kotlin-coroutines",
            summary = "Structured concurrency",
            content = "Some content",
            published = true,
            authorId = authorId,
            createdAt = Clock.System.now()
        )
        insertPost(
            title = "Compose Basics",
            slug = "compose-basics",
            summary = "UI toolkit",
            content = "Other content",
            published = true,
            authorId = authorId,
            createdAt = Clock.System.now()
        )

        val response = repository.search(query = "kotlin", page = 1, size = 10)

        assertEquals(1, response.items.size)
        assertEquals("kotlin-coroutines", response.items.first().slug)
    }

    private fun insertTag(name: String, slug: String): Long = transaction {
        TagsTable.insert {
            it[TagsTable.name] = name
            it[TagsTable.slug] = slug
        }[TagsTable.id]
    }

    private fun insertPost(
        title: String,
        slug: String,
        summary: String,
        content: String,
        published: Boolean,
        authorId: Long,
        createdAt: kotlinx.datetime.Instant
    ): Long = transaction {
        PostsTable.insert {
            it[PostsTable.title] = title
            it[PostsTable.slug] = slug
            it[PostsTable.summary] = summary
            it[PostsTable.content] = content
            it[PostsTable.coverImageUrl] = null
            it[PostsTable.published] = published
            it[PostsTable.authorId] = authorId
            it[PostsTable.createdAt] = createdAt
            it[PostsTable.updatedAt] = createdAt
        }[PostsTable.id]
    }
}
