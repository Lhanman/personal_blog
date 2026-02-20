package com.personalblog.backend.repository

import com.personalblog.backend.db.*
import com.personalblog.shared.dto.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class TagRepository {
    fun findAll(): List<TagDto> = transaction {
        TagsTable.selectAll().map { row ->
            val count = PostTagsTable.innerJoin(PostsTable)
                .select(PostTagsTable.tagId)
                .where { PostTagsTable.tagId eq row[TagsTable.id] }
                .andWhere { PostsTable.published eq true }
                .count().toInt()
            TagDto(row[TagsTable.id], row[TagsTable.name], row[TagsTable.slug], count)
        }.sortedByDescending { it.postCount }
    }

    fun findBySlug(slug: String): TagDto? = transaction {
        TagsTable.selectAll().where { TagsTable.slug eq slug }.singleOrNull()?.let { row ->
            val count = PostTagsTable.innerJoin(PostsTable)
                .select(PostTagsTable.tagId)
                .where { PostTagsTable.tagId eq row[TagsTable.id] }
                .andWhere { PostsTable.published eq true }
                .count().toInt()
            TagDto(row[TagsTable.id], row[TagsTable.name], row[TagsTable.slug], count)
        }
    }

    fun findPostsByTagSlug(slug: String, page: Int, size: Int): PagedResponse<PostDto> = transaction {
        val tag = TagsTable.selectAll().where { TagsTable.slug eq slug }.singleOrNull()
            ?: return@transaction PagedResponse(emptyList(), 0, page, size, 0)
        val tagId = tag[TagsTable.id]
        val query = (PostsTable innerJoin PostTagsTable)
            .selectAll()
            .where { PostTagsTable.tagId eq tagId }
            .andWhere { PostsTable.published eq true }
        val total = query.count()
        val items = query.orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .map { row ->
                val tags = (PostTagsTable innerJoin TagsTable)
                    .select(TagsTable.id, TagsTable.name, TagsTable.slug)
                    .where { PostTagsTable.postId eq row[PostsTable.id] }
                    .map { TagDto(it[TagsTable.id], it[TagsTable.name], it[TagsTable.slug]) }
                PostDto(
                    id = row[PostsTable.id], title = row[PostsTable.title], slug = row[PostsTable.slug],
                    summary = row[PostsTable.summary] ?: "", coverImageUrl = row[PostsTable.coverImageUrl],
                    publishedAt = row[PostsTable.createdAt].toString(), tags = tags,
                    readingTimeMinutes = maxOf(1, row[PostsTable.content].split("\\s+".toRegex()).size / 200)
                )
            }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    fun findOrCreate(name: String, slug: String): TagDto = transaction {
        TagsTable.selectAll().where { TagsTable.slug eq slug }.singleOrNull()?.let {
            TagDto(it[TagsTable.id], it[TagsTable.name], it[TagsTable.slug])
        } ?: run {
            val id = TagsTable.insert {
                it[TagsTable.name] = name
                it[TagsTable.slug] = slug
            }[TagsTable.id]
            TagDto(id, name, slug)
        }
    }
}
