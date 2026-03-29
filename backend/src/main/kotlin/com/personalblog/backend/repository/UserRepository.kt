package com.personalblog.backend.repository

import com.personalblog.backend.db.UsersTable
import com.personalblog.shared.dto.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

class UserRepository {
    data class UserRecord(val id: Long, val username: String, val email: String, val passwordHash: String, val role: String)

    private fun rowToRecord(row: ResultRow) = UserRecord(
        row[UsersTable.id], row[UsersTable.username], row[UsersTable.email],
        row[UsersTable.passwordHash], row[UsersTable.role]
    )

    fun findByEmail(email: String): UserRecord? = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.let(::rowToRecord)
    }

    fun findById(id: Long): UserDto? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.let {
            UserDto(it[UsersTable.id], it[UsersTable.username], it[UsersTable.email], it[UsersTable.role])
        }
    }

    fun create(username: String, email: String, passwordHash: String, role: String = "USER"): UserDto = transaction {
        val id = UsersTable.insert {
            it[UsersTable.username] = username
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role
            it[UsersTable.createdAt] = Clock.System.now()
        }[UsersTable.id]
        UserDto(id, username, email, role)
    }

    fun findAll(): List<UserDto> = transaction {
        UsersTable.selectAll().map { UserDto(it[UsersTable.id], it[UsersTable.username], it[UsersTable.email], it[UsersTable.role]) }
    }

    fun updateRole(id: Long, role: String): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq id }) { it[UsersTable.role] = role } > 0
    }

    fun delete(id: Long): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }
}
