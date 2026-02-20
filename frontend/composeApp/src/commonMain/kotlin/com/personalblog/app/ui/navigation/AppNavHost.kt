package com.personalblog.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.personalblog.app.data.remote.AdminRemoteDataSource
import com.personalblog.app.data.remote.CommentRemoteDataSource
import com.personalblog.app.data.remote.PostRemoteDataSource
import com.personalblog.app.data.remote.TagRemoteDataSource
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.theme.ThemeMode
import com.personalblog.app.ui.viewmodel.AuthViewModel

@Deprecated("默认导航 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
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
    modifier: Modifier = Modifier
) {
    DeprecatedUiScreen(name = "AppNavHost (Deprecated)", modifier = modifier)
}
