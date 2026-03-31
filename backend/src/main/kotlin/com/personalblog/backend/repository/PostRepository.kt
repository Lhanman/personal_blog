package com.personalblog.backend.repository

import com.personalblog.backend.db.PostTagsTable
import com.personalblog.backend.db.PostsTable
import com.personalblog.backend.db.TagsTable
import com.personalblog.backend.db.UsersTable
import com.personalblog.shared.dto.PagedResponse
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * 文章数据访问层。
 *
 * 搜索主流程：
 * 1. `search()` 先归一化查询词和分页参数
 * 2. PostgreSQL 环境走数据库侧相关性排序
 * 3. H2 / 非 PostgreSQL 环境走 Kotlin 降级评分
 * 4. 拿到命中的文章 ID 后，再统一回填标签和作者信息
 */
class PostRepository {

    private data class SearchPage(val ids: List<Long>, val total: Long)

    private data class RankedResult(val id: Long, val score: Double, val createdAt: Instant)

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

    /**
     * 搜索入口。
     *
     * 这里只负责“编排”：做输入清洗、选择搜索策略、再把命中的文章 ID
     * 转回完整的 `PostDto` 列表。真正的排序逻辑分别在 PostgreSQL 主路径
     * 和 Kotlin 降级路径里处理。
     */
    fun search(query: String, page: Int, size: Int): PagedResponse<PostDto> = transaction {
        val normalizedQuery = normalizeSearchQuery(query)
        val safePage = page.coerceAtLeast(1)
        val safeSize = size.coerceIn(1, 50)

        if (normalizedQuery.isBlank()) {
            return@transaction emptyPagedResponse(safePage, safeSize)
        }

        val resultPage = if (supportsPostgresRankedSearch()) {
            searchPostgres(normalizedQuery, safePage, safeSize)
        } else {
            searchFallback(normalizedQuery, safePage, safeSize)
        }

        if (resultPage.ids.isEmpty()) {
            return@transaction emptyPagedResponse(safePage, safeSize)
        }

        val items = fetchPostsInOrder(resultPage.ids)
        PagedResponse(items, resultPage.total, safePage, safeSize, ((resultPage.total + safeSize - 1) / safeSize).toInt())
    }

    private fun supportsPostgresRankedSearch(): Boolean =
        TransactionManager.current().db.vendor.equals("postgresql", ignoreCase = true)

    private fun normalizeSearchQuery(query: String): String =
        query.trim().replace(Regex("\\s+"), " ").lowercase()

    private fun normalizeText(text: String?): String =
        text.orEmpty().trim().replace(Regex("\\s+"), " ").lowercase()

    private fun emptyPagedResponse(page: Int, size: Int): PagedResponse<PostDto> =
        PagedResponse(emptyList(), 0, page, size, 0)

    /**
     * 搜索阶段只返回 ID 和总数；这里再按原排序顺序批量回填文章、标签和作者。
     */
    private fun fetchPostsInOrder(postIds: List<Long>): List<PostDto> {
        val rows = PostsTable.selectAll().where { PostsTable.id inList postIds }.toList()
        val rowsById = rows.associateBy { it[PostsTable.id] }
        val tagsByPost = batchGetTags(postIds)
        val authorIds = rows.mapNotNull { it[PostsTable.authorId] }.toSet()
        val authorNames = batchGetAuthorNames(authorIds)

        return postIds.mapNotNull { postId ->
            rowsById[postId]?.let { row ->
                rowToDto(row, tagsByPost[postId] ?: emptyList(), authorNames[row[PostsTable.authorId]]).copy(content = null)
            }
        }
    }

    /**
     * 非 PostgreSQL 环境的兼容路径。
     *
     * 这里不依赖 `pg_trgm` 或全文检索，而是把已发布文章拉回内存后做简单评分，
     * 主要用于 H2 测试场景，保证接口行为和排序意图与生产环境尽量一致。
     */
    private fun searchFallback(query: String, page: Int, size: Int): SearchPage {
        val rows = PostsTable.selectAll()
            .where { PostsTable.published eq true }
            .toList()

        val ranked = rows.mapNotNull { row ->
            val score = calculateFallbackScore(row, query)
            if (score > 0.0) RankedResult(row[PostsTable.id], score, row[PostsTable.createdAt]) else null
        }.sortedWith(compareByDescending<RankedResult> { it.score }.thenByDescending { it.createdAt })

        val total = ranked.size.toLong()
        val offset = ((page - 1) * size).coerceAtLeast(0)
        val ids = ranked.drop(offset).take(size).map { it.id }
        return SearchPage(ids, total)
    }

    /**
     * 降级评分规则：标题权重最高，摘要次之，正文最低；同一字段里完全命中、前缀命中、
     * 子串命中依次递减，这样可以尽量模拟“相关性优先”的排序体验。
     */
    private fun calculateFallbackScore(row: ResultRow, query: String): Double {
        val title = normalizeText(row[PostsTable.title])
        val summary = normalizeText(row[PostsTable.summary])
        val content = normalizeText(row[PostsTable.content])

        return scoreField(title, query, exactWeight = 120.0, prefixWeight = 60.0, containsWeight = 36.0) +
            scoreField(summary, query, exactWeight = 40.0, prefixWeight = 24.0, containsWeight = 18.0) +
            scoreField(content, query, exactWeight = 20.0, prefixWeight = 12.0, containsWeight = 10.0)
    }

    private fun scoreField(
        fieldValue: String,
        query: String,
        exactWeight: Double,
        prefixWeight: Double,
        containsWeight: Double
    ): Double {
        if (fieldValue.isBlank()) return 0.0

        var score = 0.0
        if (fieldValue == query) score += exactWeight
        if (fieldValue.startsWith(query)) score += prefixWeight

        val containsIndex = fieldValue.indexOf(query)
        if (containsIndex >= 0) {
            score += containsWeight
            score += 1.0 / (containsIndex + 1)
            score += query.length.toDouble() / fieldValue.length.coerceAtLeast(query.length)
        }

        query.split(" ")
            .filter { it.isNotBlank() && it != query }
            .forEachIndexed { index, token ->
                if (fieldValue.contains(token)) {
                    score += containsWeight / (4 + index)
                }
            }

        return score
    }

    /**
     * PostgreSQL 主搜索路径。
     *
     * SQL 里把三类信号合并成统一 score：
     * - `search_vector` 的全文命中与 rank
     * - `pg_trgm` 的相似度 / 词相似度
     * - 标题、摘要、正文的 LIKE 兜底加权
     *
     * 最终按 `score DESC, created_at DESC` 排序，既保证相关性优先，又保留同分时的新文章优势。
     */
    private fun searchPostgres(query: String, page: Int, size: Int): SearchPage {
        val sql = """
            WITH ranked_posts AS (
                SELECT
                    p.id,
                    p.created_at,
                    (
                        CASE
                            WHEN p.search_vector @@ websearch_to_tsquery('simple', ?) THEN ts_rank_cd(p.search_vector, websearch_to_tsquery('simple', ?)) * 8
                            ELSE 0
                        END
                        + similarity(p.search_text, ?) * 4
                        + word_similarity(?, p.search_text) * 3
                        + CASE WHEN lower(p.title) LIKE ? ESCAPE '\\' THEN 6 ELSE 0 END
                        + CASE WHEN lower(coalesce(p.summary, '')) LIKE ? ESCAPE '\\' THEN 3 ELSE 0 END
                        + CASE WHEN lower(p.content) LIKE ? ESCAPE '\\' THEN 1 ELSE 0 END
                    ) AS score
                FROM posts p
                WHERE p.published = TRUE
                  AND (
                    p.search_vector @@ websearch_to_tsquery('simple', ?)
                    OR p.search_text % ?
                    OR p.search_text LIKE ? ESCAPE '\\'
                  )
            )
            SELECT
                id,
                (SELECT COUNT(*) FROM ranked_posts WHERE score > 0) AS total
            FROM ranked_posts
            WHERE score > 0
            ORDER BY score DESC, created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        val connection = TransactionManager.current().connection
        val likePattern = "%${escapeLike(query)}%"
        val statement = connection.prepareStatement(sql, false)

        try {
            bindPostgresSearchParameters(statement, query, likePattern, size, ((page - 1) * size).toLong())
            val resultSet = statement.executeQuery()
            val ids = mutableListOf<Long>()
            var total = 0L

            resultSet.use {
                while (it.next()) {
                    ids += it.getLong("id")
                    total = it.getLong("total")
                }
            }

            return SearchPage(ids, total)
        } finally {
            statement.closeIfPossible()
        }
    }

    private fun bindPostgresSearchParameters(statement: PreparedStatementApi, query: String, likePattern: String, size: Int, offset: Long) {
        statement[1] = query
        statement[2] = query
        statement[3] = query
        statement[4] = query
        statement[5] = likePattern
        statement[6] = likePattern
        statement[7] = likePattern
        statement[8] = query
        statement[9] = query
        statement[10] = likePattern
        statement[11] = size
        statement[12] = offset
    }

    private fun escapeLike(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character) {
                '\\', '%', '_' -> append('\\').append(character)
                else -> append(character)
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
            PostTagsTable.insert {
                it[PostTagsTable.postId] = id
                it[PostTagsTable.tagId] = tagId
            }
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
            PostTagsTable.insert {
                it[PostTagsTable.postId] = id
                it[PostTagsTable.tagId] = tagId
            }
        }
        findById(id)
    }

    fun delete(id: Long): Boolean = transaction {
        PostsTable.deleteWhere { PostsTable.id eq id } > 0
    }
}
