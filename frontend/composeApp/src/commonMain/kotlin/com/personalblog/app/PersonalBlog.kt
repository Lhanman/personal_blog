package com.personalblog.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.personalblog.app.ui.navigation.AppNavHost
import com.personalblog.app.ui.theme.DarkColorScheme
import com.personalblog.app.ui.theme.LightColorScheme
import com.personalblog.app.ui.theme.isSystemInDarkMode
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun PersonalBlog() {
    val colorScheme = if (isSystemInDarkMode()) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme) {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}
