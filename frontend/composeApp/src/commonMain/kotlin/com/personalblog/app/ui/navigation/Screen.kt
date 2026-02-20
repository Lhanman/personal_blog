package com.personalblog.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PostDetail : Screen("post/{id}") {
        fun createRoute(id: Long) = "post/$id"
    }
    object Search : Screen("search")
    object Tags : Screen("tags")
    object TagPosts : Screen("tag/{slug}") {
        fun createRoute(slug: String) = "tag/$slug"
    }
    object About : Screen("about")
    object Login : Screen("login")
    object AdminPostList : Screen("admin/posts")
    object AdminPostEdit : Screen("admin/posts/edit/{id}") {
        fun createRoute(id: Long?) = if (id != null) "admin/posts/edit/$id" else "admin/posts/edit/new"
    }
    object AdminUserList : Screen("admin/users")
}
