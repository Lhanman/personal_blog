package com.personalblog.backend.repository

import com.personalblog.backend.db.*
import com.personalblog.shared.dto.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

class PostRepository {

    private fun rowToDto(row: ResultRow, tags: List<TagDto> = emptyList(), authorName: String? = null): PostDto {
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
            published = row[PostsTable.published],
            authorName = authorName
        )
    }

    private fun getTagsForPost(postId: Long): List<TagDto> = transaction {
        (PostTagsTable innerJoin TagsTable)
            .select(TagsTable.id, TagsTable.name, TagsTable.slug)
            .where { PostTagsTable.postId eq postId }
            .map { TagDto(it[TagsTable.id], it[TagsTable.name], it[TagsTable.slug]) }
    }

    private fun batchGetTags(postIds: List<Long>): Map<Long, List<TagDto>> {
        if (postIds.isEmpty()) return emptyMap()
        return (PostTagsTable innerJoin TagsTable)
            .select(PostTagsTable.postId, TagsTable.id, TagsTable.name, TagsTable.slug)
            .where { PostTagsTable.postId inList postIds }
            .groupBy({ it[PostTagsTable.postId] }) { TagDto(it[TagsTable.id], it[TagsTable.name], it[TagsTable.slug]) }
    }

    private fun batchGetAuthorNames(authorIds: Set<Long>): Map<Long, String> {
        if (authorIds.isEmpty()) return emptyMap()
        return UsersTable.select(UsersTable.id, UsersTable.username)
            .where { UsersTable.id inList authorIds }
            .associate { it[UsersTable.id] to it[UsersTable.username] }
    }

    private fun getAuthorName(authorId: Long?): String? = transaction {
        if (authorId == null) return@transaction null
        UsersTable.select(UsersTable.username)
            .where { UsersTable.id eq authorId }
            .singleOrNull()?.get(UsersTable.username)
    }

    fun findAll(page: Int, size: Int, publishedOnly: Boolean = true): PagedResponse<PostDto> = transaction {
        val query = PostsTable.selectAll()
        if (publishedOnly) query.where { PostsTable.published eq true }
        val total = query.count()
        val rows = query
            .orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .toList()

        val postIds = rows.map { it[PostsTable.id] }
        val tagsByPost = batchGetTags(postIds)
        val authorIds = rows.mapNotNull { it[PostsTable.authorId] }.toSet()
        val authorNames = batchGetAuthorNames(authorIds)

        val items = rows.map { row ->
            rowToDto(row, tagsByPost[row[PostsTable.id]] ?: emptyList(), authorNames[row[PostsTable.authorId]]).copy(content = null)
        }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    fun findById(id: Long): PostDto? = transaction {
        PostsTable.selectAll().where { PostsTable.id eq id }.singleOrNull()?.let { row ->
            val authorName = getAuthorName(row[PostsTable.authorId])
            rowToDto(row, getTagsForPost(id), authorName)
        }
    }

    fun findBySlug(slug: String): PostDto? = transaction {
        PostsTable.selectAll().where { PostsTable.slug eq slug }.singleOrNull()?.let { row ->
            val authorName = getAuthorName(row[PostsTable.authorId])
            rowToDto(row, getTagsForPost(row[PostsTable.id]), authorName)
        }
    }

    fun search(query: String, page: Int, size: Int): PagedResponse<PostDto> = transaction {
        // 使用 ILIKE 进行搜索（兼容 H2 测试数据库）
        // 生产环境使用 PostgreSQL 的 search_vector 全文索引
        val q = PostsTable.selectAll().where {
            (PostsTable.title.lowerCase() like "%${query.lowercase()}%") or
            (PostsTable.summary.lowerCase() like "%${query.lowercase()}%") or
            (PostsTable.content.lowerCase() like "%${query.lowercase()}%")
        }.andWhere { PostsTable.published eq true }

        val total = q.count()
        val items = q.orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .map { row ->
                val authorName = getAuthorName(row[PostsTable.authorId])
                rowToDto(row, getTagsForPost(row[PostsTable.id]), authorName).copy(content = null)
            }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    // PostgreSQL 全文搜索版本（仅在生产环境使用）
    fun searchWithFullText(query: String, page: Int, size: Int): PagedResponse<PostDto> = transaction {
        val searchQuery = query.trim().split("\\s+".toRegex()).joinToString(" & ")

        val q = PostsTable.selectAll().where {
            CustomSqlExpressionBuilder.searchVectorMatch(searchQuery)
        }.andWhere { PostsTable.published eq true }

        val total = q.count()
        val items = q.orderBy(PostsTable.createdAt, SortOrder.DESC)
            .limit(size, offset = ((page - 1) * size).toLong())
            .map { row ->
                val authorName = getAuthorName(row[PostsTable.authorId])
                rowToDto(row, getTagsForPost(row[PostsTable.id]), authorName).copy(content = null)
            }
        PagedResponse(items, total, page, size, ((total + size - 1) / size).toInt())
    }

    // 自定义 SQL 表达式构建器
    private object CustomSqlExpressionBuilder {
        fun searchVectorMatch(query: String): Op<Boolean> {
            return object : Op<Boolean>() {
                override fun toQueryBuilder(queryBuilder: QueryBuilder) {
                    queryBuilder.append("search_vector @@ to_tsquery('english', '")
                    queryBuilder.append(query.replace("'", "''"))
                    queryBuilder.append("')")
                }
            }
        }
    }

    fun create(title: String, slug: String, summary: String, content: String, coverImageUrl: String?, tagIds: List<Long>, authorId: Long, published: Boolean = false): PostDto = transaction {
        val now = Clock.System.now()
        val id = PostsTable.insert {
            it[PostsTable.title] = title
            it[PostsTable.slug] = slug
            it[PostsTable.summary] = summary
            it[PostsTable.content] = content
            it[PostsTable.coverImageUrl] = coverImageUrl
            it[PostsTable.published] = published
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
