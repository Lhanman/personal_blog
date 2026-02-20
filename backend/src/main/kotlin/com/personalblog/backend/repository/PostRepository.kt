package com.personalblog.backend.repository

import com.personalblog.backend.db.*
import com.personalblog.shared.dto.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

class PostRepository {

    private fun rowToDto(row: ResultRow, tags: List<TagDto> = emptyList()): PostDto {
        val content = row[PostsTable.content]
        val wordCount = content.split("\\s+".toRegex()).size
        val readingTime = maxOf(1, wordCount / 200)
        return PostDto(
            id = row[PostsTable.id],
            title = row[PostsTable.title],
            slug = row[PostsTable.slug],
            summary = row[PostsTable.summary] ?: "",
            content = content,
            coverImageUrl = row[PostsTable.coverImageUrl],
            publishedAt = row[PostsTable.createdAt].toString(),
            updatedAt = row[PostsTable.updatedAt].toString(),
            tags = tags,
            readingTimeMinutes = readingTime,
            published = row[PostsTable.published]
        )
    }

    private fun getTagsForPost(postId: Long): List<TagDto> = transaction {
        (PostTagsTable innerJoin TagsTable)
            .select(TagsTable.id, TagsTable.name, TagsTable.slug)
            .where { PostTagsTable.postId eq postId }
            .map { TagDto(it[TagsTable.id], it[TagsTable.name], it[TagsTable.slug]) }
    }

    fun findAll(page: Int, size: Int, publishedOnly: Boolean = true): PagedResponse<PostDto> = transaction {
        val query = PostsTable.selectAll()
        if (publishedOnly) query.where { PostsTable.published eq true }
        val total = query.count()
        val items = query
            .orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .map { row ->
                val tags = getTagsForPost(row[PostsTable.id])
                rowToDto(row, tags).copy(content = null)
            }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    fun findById(id: Long): PostDto? = transaction {
        PostsTable.selectAll().where { PostsTable.id eq id }.singleOrNull()?.let { row ->
            rowToDto(row, getTagsForPost(id))
        }
    }

    fun findBySlug(slug: String): PostDto? = transaction {
        PostsTable.selectAll().where { PostsTable.slug eq slug }.singleOrNull()?.let { row ->
            rowToDto(row, getTagsForPost(row[PostsTable.id]))
        }
    }

    fun search(query: String, page: Int, size: Int): PagedResponse<PostDto> = transaction {
        val q = PostsTable.selectAll().where {
            (PostsTable.title.lowerCase() like "%${query.lowercase()}%") or
            (PostsTable.summary.lowerCase() like "%${query.lowercase()}%") or
            (PostsTable.content.lowerCase() like "%${query.lowercase()}%")
        }.andWhere { PostsTable.published eq true }
        val total = q.count()
        val items = q.orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .map { row -> rowToDto(row, getTagsForPost(row[PostsTable.id])).copy(content = null) }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    fun create(title: String, slug: String, summary: String, content: String, coverImageUrl: String?, tagIds: List<Long>, authorId: Long): PostDto = transaction {
        val now = Clock.System.now()
        val id = PostsTable.insert {
            it[PostsTable.title] = title
            it[PostsTable.slug] = slug
            it[PostsTable.summary] = summary
            it[PostsTable.content] = content
            it[PostsTable.coverImageUrl] = coverImageUrl
            it[PostsTable.published] = false
            it[PostsTable.authorId] = authorId
            it[PostsTable.createdAt] = now
            it[PostsTable.updatedAt] = now
        }[PostsTable.id]
        tagIds.forEach { tagId ->
            PostTagsTable.insert { it[PostTagsTable.postId] = id; it[PostTagsTable.tagId] = tagId }
        }
        findById(id)!!
    }

    fun update(id: Long, title: String, slug: String, summary: String, content: String, coverImageUrl: String?, tagIds: List<Long>, published: Boolean): PostDto? = transaction {
        PostsTable.update({ PostsTable.id eq id }) {
            it[PostsTable.title] = title
            it[PostsTable.slug] = slug
            it[PostsTable.summary] = summary
            it[PostsTable.content] = content
            it[PostsTable.coverImageUrl] = coverImageUrl
            it[PostsTable.published] = published
            it[PostsTable.updatedAt] = Clock.System.now()
        }
        PostTagsTable.deleteWhere { PostTagsTable.postId eq id }
        tagIds.forEach { tagId ->
            PostTagsTable.insert { it[PostTagsTable.postId] = id; it[PostTagsTable.tagId] = tagId }
        }
        findById(id)
    }

    fun delete(id: Long): Boolean = transaction {
        PostsTable.deleteWhere { PostsTable.id eq id } > 0
    }
}
