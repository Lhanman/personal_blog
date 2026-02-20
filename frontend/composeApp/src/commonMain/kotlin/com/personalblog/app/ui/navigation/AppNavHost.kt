package com.personalblog.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            // BlogListScreen()
        }
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            // BlogReaderScreen()
        }
        composable(Screen.Search.route) {
            // SearchScreen()
        }
        composable(Screen.Tags.route) {
            // TagsWallScreen()
        }
        composable(
            route = Screen.TagPosts.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) {
            // TagPostsScreen()
        }
        composable(Screen.About.route) {
            // AboutScreen()
        }
        composable(Screen.Login.route) {
            // LoginScreen()
        }
        composable(Screen.AdminPostList.route) {
            // AdminPostListScreen()
        }
        composable(
            route = Screen.AdminPostEdit.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            // AdminPostEditScreen()
        }
        composable(Screen.AdminUserList.route) {
            // AdminUserListScreen()
        }
    }
}
