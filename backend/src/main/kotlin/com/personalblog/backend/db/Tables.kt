package com.personalblog.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20).default("USER")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PostsTable : Table("posts") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 500)
    val slug = varchar("slug", 500).uniqueIndex()
    val summary = text("summary").nullable()
    val content = text("content")
    val coverImageUrl = text("cover_image_url").nullable()
    val published = bool("published").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val authorId = long("author_id").references(UsersTable.id).nullable()
    override val primaryKey = PrimaryKey(id)
}

object TagsTable : Table("tags") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val slug = varchar("slug", 100).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

object PostTagsTable : Table("post_tags") {
    val postId = long("post_id").references(PostsTable.id)
    val tagId = long("tag_id").references(TagsTable.id)
    override val primaryKey = PrimaryKey(postId, tagId)
}

object CommentsTable : Table("comments") {
    val id = long("id").autoIncrement()
    val postId = long("post_id").references(PostsTable.id)
    val userId = long("user_id").references(UsersTable.id).nullable()
    val content = text("content")
    val createdAt = timestamp("created_at")
    val isDeleted = bool("is_deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}
