package com.personalblog.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.personalblog.app.data.remote.AdminRemoteDataSource
import com.personalblog.app.data.remote.CommentRemoteDataSource
import com.personalblog.app.data.remote.PostRemoteDataSource
import com.personalblog.app.data.remote.TagRemoteDataSource
import com.personalblog.app.ui.components.AppHeader
import com.personalblog.app.ui.screen.AboutScreen
import com.personalblog.app.ui.screen.AdminPostEditScreen
import com.personalblog.app.ui.screen.AdminPostListScreen
import com.personalblog.app.ui.screen.AdminUserListScreen
import com.personalblog.app.ui.screen.BlogListScreen
import com.personalblog.app.ui.screen.BlogReaderScreen
import com.personalblog.app.ui.screen.HomeScreen
import com.personalblog.app.ui.screen.LoginScreen
import com.personalblog.app.ui.screen.SearchScreen
import com.personalblog.app.ui.screen.TagPostsScreen
import com.personalblog.app.ui.screen.TagsWallScreen
import com.personalblog.app.ui.theme.ThemeMode
import com.personalblog.app.ui.viewmodel.AdminViewModel
import com.personalblog.app.ui.viewmodel.AuthViewModel
import com.personalblog.app.ui.viewmodel.BlogListViewModel
import com.personalblog.app.ui.viewmodel.BlogReaderViewModel
import com.personalblog.app.ui.viewmodel.CommentViewModel
import com.personalblog.app.ui.viewmodel.HomeViewModel
import com.personalblog.app.ui.viewmodel.SearchViewModel
import com.personalblog.app.ui.viewmodel.TagPostsViewModel
import com.personalblog.app.ui.viewmodel.TagsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    postDataSource: PostRemoteDataSource,
    tagDataSource: TagRemoteDataSource,
    commentDataSource: CommentRemoteDataSource,
    adminDataSource: AdminRemoteDataSource,
    authViewModel: AuthViewModel,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    windowChrome: AppWindowChrome,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val headerState = AppHeaderState(
        currentRoute = currentRoute,
        screenChrome = resolveScreenChrome(currentRoute),
        canNavigateBack = previousRoute != null
    )
    val adminViewModel = remember(adminDataSource) { AdminViewModel(postDataSource, tagDataSource, adminDataSource) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppHeader(
            navController = navController,
            headerState = headerState,
            isLoggedIn = authState.isLoggedIn,
            isAdmin = authState.role == "ADMIN",
            themeMode = themeMode,
            onThemeModeSelected = onThemeModeSelected,
            onLogout = authViewModel::logout,
            windowChrome = windowChrome,
            onBackClick = {
                val didPop = if (headerState.canNavigateBack) {
                    navController.popBackStack()
                } else {
                    false
                }
                if (!didPop) {
                    headerState.fallbackRoute?.let { fallbackRoute ->
                        navController.navigate(fallbackRoute) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                }
            }
        )

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                val vm = remember(postDataSource, tagDataSource) { HomeViewModel(postDataSource, tagDataSource) }
                HomeScreen(
                    viewModel = vm,
                    onPostClick = { navController.navigate(Screen.PostDetail.createRoute(it)) },
                    onAllPostsClick = { navController.navigate(Screen.Posts.route) { launchSingleTop = true } }
                )
            }

            composable(Screen.Posts.route) {
                val vm = remember(postDataSource) { BlogListViewModel(postDataSource) }
                BlogListScreen(
                    viewModel = vm,
                    onPostClick = { navController.navigate(Screen.PostDetail.createRoute(it)) },
                    onAllPostsClick = { navController.navigate(Screen.Posts.route) { launchSingleTop = true } }
                )
            }

            composable(Screen.Search.route) {
                val vm = remember(postDataSource) { SearchViewModel(postDataSource) }
                SearchScreen(viewModel = vm, onPostClick = { navController.navigate(Screen.PostDetail.createRoute(it)) })
            }

            composable(Screen.Tags.route) {
                val vm = remember(tagDataSource) { TagsViewModel(tagDataSource) }
                TagsWallScreen(viewModel = vm, onTagClick = { navController.navigate(Screen.TagPosts.createRoute(it)) })
            }

            composable(Screen.About.route) {
                AboutScreen()
            }

            composable(Screen.Login.route) {
                LoginScreen(viewModel = authViewModel, onLoginSuccess = { navController.popBackStack() })
            }

            composable(Screen.PostDetail.route) { backStack ->
                val postId = backStack.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                val readerVm = remember(postId) { BlogReaderViewModel(postDataSource, postId) }
                val commentVm = remember(postId) { CommentViewModel(commentDataSource, postId) }
                BlogReaderScreen(
                    viewModel = readerVm,
                    commentViewModel = commentVm,
                    isLoggedIn = authState.isLoggedIn,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onTagClick = { navController.navigate(Screen.TagPosts.createRoute(it)) }
                )
            }

            composable(Screen.TagPosts.route) { backStack ->
                val slug = backStack.arguments?.getString("slug") ?: return@composable
                val vm = remember(slug) { TagPostsViewModel(tagDataSource, slug) }
                TagPostsScreen(viewModel = vm, onPostClick = { navController.navigate(Screen.PostDetail.createRoute(it)) })
            }

            composable(Screen.AdminPostList.route) {
                AdminPostListScreen(
                    viewModel = adminViewModel,
                    onCreatePost = { navController.navigate(Screen.AdminPostEdit.createRoute(null)) },
                    onEditPost = { navController.navigate(Screen.AdminPostEdit.createRoute(it)) }
                )
            }

            composable(Screen.AdminPostEdit.route) { backStack ->
                val idStr = backStack.arguments?.getString("id")
                val postId = if (idStr == "new") null else idStr?.toLongOrNull()
                AdminPostEditScreen(viewModel = adminViewModel, postId = postId, onSaved = { navController.popBackStack() })
            }

            composable(Screen.AdminUserList.route) {
                AdminUserListScreen(viewModel = adminViewModel)
            }
        }
    }
}
