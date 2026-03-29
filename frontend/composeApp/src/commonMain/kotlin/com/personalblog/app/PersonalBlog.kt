package com.personalblog.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.personalblog.app.data.remote.AdminRemoteDataSource
import com.personalblog.app.data.remote.ApiClient
import com.personalblog.app.data.remote.AuthRemoteDataSource
import com.personalblog.app.data.remote.CommentRemoteDataSource
import com.personalblog.app.data.remote.PostRemoteDataSource
import com.personalblog.app.data.remote.TagRemoteDataSource
import com.personalblog.app.data.repository.ThemeRepository
import com.personalblog.app.data.repository.TokenRepository
import com.personalblog.app.logging.LoggerFactory
import com.personalblog.app.logging.LoggingRuntime
import com.personalblog.app.ui.navigation.AppWindowChrome
import com.personalblog.app.ui.navigation.AppNavHost
import com.personalblog.app.ui.theme.AppTypography
import com.personalblog.app.ui.theme.DarkColorScheme
import com.personalblog.app.ui.theme.LightColorScheme
import com.personalblog.app.ui.theme.ThemeMode
import com.personalblog.app.ui.theme.isSystemInDarkMode
import com.personalblog.app.ui.viewmodel.AuthViewModel
import com.personalblog.app.ui.viewmodel.ThemeViewModel
import com.russhwolf.settings.Settings
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun PersonalBlog() {
    PersonalBlog(windowChrome = AppWindowChrome())
}

@Composable
fun PersonalBlog(
    windowChrome: AppWindowChrome = AppWindowChrome()
) {
    LoggingRuntime.ensureInitialized()
    val logger = remember { LoggerFactory.getLogger("PersonalBlog") }
    val settings = remember { Settings() }
    val tokenRepository = remember(settings) { TokenRepository(settings) }
    val themeRepository = remember(settings) { ThemeRepository(settings) }

    val apiClient = remember(tokenRepository) {
        ApiClient(tokenProvider = tokenRepository::getToken)
    }
    val postDataSource = remember(apiClient) { PostRemoteDataSource(apiClient) }
    val tagDataSource = remember(apiClient) { TagRemoteDataSource(apiClient) }
    val commentDataSource = remember(apiClient) { CommentRemoteDataSource(apiClient) }
    val authDataSource = remember(apiClient) { AuthRemoteDataSource(apiClient) }
    val adminDataSource = remember(apiClient) { AdminRemoteDataSource(apiClient) }

    val authViewModel = remember(authDataSource, tokenRepository) {
        AuthViewModel(authDataSource, tokenRepository)
    }
    val themeViewModel = remember(themeRepository) { ThemeViewModel(themeRepository) }

    val themeState by themeViewModel.state.collectAsState()
    val useDarkTheme = when (themeState.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkMode()
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography()
    ) {
        LaunchedEffect(themeState.mode) {
            logger.info(
                message = "rendering personal blog shell",
                feature = "startup",
                extras = mapOf("themeMode" to themeState.mode.name)
            )
        }
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            AppNavHost(
                navController = navController,
                postDataSource = postDataSource,
                tagDataSource = tagDataSource,
                commentDataSource = commentDataSource,
                adminDataSource = adminDataSource,
                authViewModel = authViewModel,
                themeMode = themeState.mode,
                onThemeModeSelected = themeViewModel::setThemeMode,
                windowChrome = windowChrome
            )
        }
    }
}
