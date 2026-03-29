package com.personalblog.app.ui.navigation

import org.jetbrains.compose.resources.StringResource
import personal_blog.composeapp.generated.resources.Res
import personal_blog.composeapp.generated.resources.screen_about
import personal_blog.composeapp.generated.resources.screen_admin_edit_post
import personal_blog.composeapp.generated.resources.screen_admin_posts
import personal_blog.composeapp.generated.resources.screen_admin_users
import personal_blog.composeapp.generated.resources.screen_article
import personal_blog.composeapp.generated.resources.screen_home
import personal_blog.composeapp.generated.resources.screen_login
import personal_blog.composeapp.generated.resources.screen_posts
import personal_blog.composeapp.generated.resources.screen_search
import personal_blog.composeapp.generated.resources.screen_tag_posts
import personal_blog.composeapp.generated.resources.screen_tags

data class ScreenChrome(
    val titleRes: StringResource,
    val isTopLevel: Boolean,
    val parentRoute: String? = null
) {
    val fallbackRoute: String?
        get() = parentRoute
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Posts : Screen("posts")
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

private val defaultScreenChrome = ScreenChrome(Res.string.screen_home, isTopLevel = true)

private val screenChromeByRoute = mapOf(
    Screen.Home.route to ScreenChrome(Res.string.screen_home, isTopLevel = true),
    Screen.Posts.route to ScreenChrome(Res.string.screen_posts, isTopLevel = true),
    Screen.Search.route to ScreenChrome(Res.string.screen_search, isTopLevel = true),
    Screen.Tags.route to ScreenChrome(Res.string.screen_tags, isTopLevel = true),
    Screen.About.route to ScreenChrome(Res.string.screen_about, isTopLevel = true),
    Screen.AdminPostList.route to ScreenChrome(Res.string.screen_admin_posts, isTopLevel = true),
    Screen.AdminUserList.route to ScreenChrome(Res.string.screen_admin_users, isTopLevel = true),
    Screen.PostDetail.route to ScreenChrome(
        titleRes = Res.string.screen_article,
        isTopLevel = false,
        parentRoute = Screen.Posts.route
    ),
    Screen.TagPosts.route to ScreenChrome(
        titleRes = Res.string.screen_tag_posts,
        isTopLevel = false,
        parentRoute = Screen.Tags.route
    ),
    Screen.Login.route to ScreenChrome(
        titleRes = Res.string.screen_login,
        isTopLevel = false,
        parentRoute = Screen.Home.route
    ),
    Screen.AdminPostEdit.route to ScreenChrome(
        titleRes = Res.string.screen_admin_edit_post,
        isTopLevel = false,
        parentRoute = Screen.AdminPostList.route
    )
)

fun resolveScreenChrome(route: String?): ScreenChrome = screenChromeByRoute[route] ?: defaultScreenChrome
