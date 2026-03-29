package com.personalblog.backend

import com.personalblog.backend.db.CommentsTable
import com.personalblog.backend.db.PostTagsTable
import com.personalblog.backend.db.PostsTable
import com.personalblog.backend.db.TagsTable
import com.personalblog.backend.db.UsersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object TestDb {
    fun connect() {
        Database.connect(
            url = "jdbc:h2:mem:test-${System.nanoTime()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(UsersTable, PostsTable, TagsTable, PostTagsTable, CommentsTable)
        }
    }

    fun cleanup() {
        transaction {
            SchemaUtils.drop(CommentsTable, PostTagsTable, PostsTable, TagsTable, UsersTable)
        }
    }

    fun insertUser(
        username: String = "tester",
        email: String = "tester@example.com",
        role: String = "USER"
    ): Long = transaction {
        UsersTable.insert {
            it[UsersTable.username] = username
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = "hash"
            it[UsersTable.role] = role
            it[UsersTable.createdAt] = Clock.System.now()
        }[UsersTable.id]
    }
}
