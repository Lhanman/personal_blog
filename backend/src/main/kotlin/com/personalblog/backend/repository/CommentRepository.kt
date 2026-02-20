package com.personalblog.backend.repository

import com.personalblog.backend.db.*
import com.personalblog.shared.dto.CommentDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

class CommentRepository {
    fun findByPostId(postId: Long): List<CommentDto> = transaction {
        (CommentsTable innerJoin UsersTable)
            .selectAll()
            .where { CommentsTable.postId eq postId }
            .andWhere { CommentsTable.isDeleted eq false }
            .orderBy(CommentsTable.createdAt, SortOrder.ASC)
            .map {
                CommentDto(
                    id = it[CommentsTable.id],
                    postId = it[CommentsTable.postId],
                    authorName = it[UsersTable.username],
                    content = it[CommentsTable.content],
                    createdAt = it[CommentsTable.createdAt].toString()
                )
            }
    }

    fun create(postId: Long, userId: Long, content: String): CommentDto = transaction {
        val id = CommentsTable.insert {
            it[CommentsTable.postId] = postId
            it[CommentsTable.userId] = userId
            it[CommentsTable.content] = content
            it[CommentsTable.createdAt] = Clock.System.now()
        }[CommentsTable.id]
        val username = UsersTable.selectAll().where { UsersTable.id eq userId }.single()[UsersTable.username]
        CommentDto(id, postId, username, content, java.time.OffsetDateTime.now().toString())
    }

    fun softDelete(id: Long): Boolean = transaction {
        CommentsTable.update({ CommentsTable.id eq id }) { it[CommentsTable.isDeleted] = true } > 0
    }
}
