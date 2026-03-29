package com.personalblog.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.personalblog.app.ui.navigation.AppHeaderState
import com.personalblog.app.ui.navigation.AppWindowChrome
import com.personalblog.app.ui.navigation.Screen
import com.personalblog.app.ui.theme.ThemeMode
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import personal_blog.composeapp.generated.resources.Res
import personal_blog.composeapp.generated.resources.header_back
import personal_blog.composeapp.generated.resources.ic_monitor
import personal_blog.composeapp.generated.resources.ic_moon
import personal_blog.composeapp.generated.resources.ic_sun
import personal_blog.composeapp.generated.resources.nav_about
import personal_blog.composeapp.generated.resources.nav_admin
import personal_blog.composeapp.generated.resources.nav_home
import personal_blog.composeapp.generated.resources.nav_login
import personal_blog.composeapp.generated.resources.nav_logout
import personal_blog.composeapp.generated.resources.nav_posts
import personal_blog.composeapp.generated.resources.nav_search
import personal_blog.composeapp.generated.resources.nav_tags

private val AccentColor = Color(0xFFFF6B01)

@Composable
fun AppHeader(
    navController: NavHostController,
    headerState: AppHeaderState,
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    windowChrome: AppWindowChrome,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val currentRoute = headerState.currentRoute
    val headerBackground = if (windowChrome.immersiveHeader) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.background
    }
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = if (windowChrome.immersiveHeader) 1600.dp else 1100.dp)
                .fillMaxWidth()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isMobile = maxWidth < 768.dp
                val showDesktopBackButton = windowChrome.immersiveHeader && !isMobile && headerState.showBackButton

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp + windowChrome.titleBarStartInset,
                                top = (if (windowChrome.immersiveHeader) 12.dp else 14.dp) + windowChrome.titleBarTopInset,
                                end = 20.dp,
                                bottom = if (windowChrome.immersiveHeader) 12.dp else 14.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showDesktopBackButton) {
                            HeaderIconButton(
                                onClick = {
                                    menuExpanded = false
                                    onBackClick()
                                },
                                contentDescription = stringResource(Res.string.header_back)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = stringResource(headerState.screenChrome.titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        } else {
                            BrandLabel {
                                menuExpanded = false
                                navController.navigate(Screen.Home.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }

                        HeaderDragSpacer(
                            windowChrome = windowChrome,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(horizontal = 12.dp)
                        )

                        if (isMobile) {
                            HeaderIconButton(
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(Screen.Search.route) { launchSingleTop = true }
                                },
                                contentDescription = stringResource(Res.string.nav_search)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (currentRoute == Screen.Search.route) AccentColor else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.width(4.dp))
                            ThemeToggleIcon(themeMode = themeMode, onThemeModeSelected = onThemeModeSelected)
                            Spacer(Modifier.width(4.dp))

                            HeaderIconButton(
                                onClick = { menuExpanded = !menuExpanded },
                                contentDescription = if (menuExpanded) "Close Menu" else "Open Menu"
                            ) {
                                Icon(
                                    imageVector = if (menuExpanded) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            DesktopNavigationRow(
                                currentRoute = currentRoute,
                                isAdmin = isAdmin,
                                isLoggedIn = isLoggedIn,
                                onLogout = onLogout,
                                onNavigate = { route ->
                                    menuExpanded = false
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                themeMode = themeMode,
                                onThemeModeSelected = onThemeModeSelected,
                                windowChrome = windowChrome,
                                borderColor = borderColor
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderColor)
                    )

                    if (isMobile) {
                        AnimatedVisibility(
                            visible = menuExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(headerBackground)
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                MobileNavItem(
                                    label = stringResource(Res.string.nav_home),
                                    isActive = currentRoute == Screen.Home.route,
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
                                    }
                                )
                                MobileNavItem(
                                    label = stringResource(Res.string.nav_posts),
                                    isActive = currentRoute == Screen.Posts.route,
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Screen.Posts.route) { launchSingleTop = true }
                                    }
                                )
                                MobileNavItem(
                                    label = stringResource(Res.string.nav_tags),
                                    isActive = currentRoute == Screen.Tags.route,
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Screen.Tags.route) { launchSingleTop = true }
                                    }
                                )
                                MobileNavItem(
                                    label = stringResource(Res.string.nav_about),
                                    isActive = currentRoute == Screen.About.route,
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Screen.About.route) { launchSingleTop = true }
                                    }
                                )
                                if (isAdmin) {
                                    MobileNavItem(
                                        label = stringResource(Res.string.nav_admin),
                                        isActive = currentRoute?.startsWith("admin") == true,
                                        onClick = {
                                            menuExpanded = false
                                            navController.navigate(Screen.AdminPostList.route) { launchSingleTop = true }
                                        }
                                    )
                                }
                                if (isLoggedIn) {
                                    MobileNavItem(
                                        label = stringResource(Res.string.nav_logout),
                                        isActive = false,
                                        onClick = {
                                            menuExpanded = false
                                            onLogout()
                                        }
                                    )
                                } else {
                                    MobileNavItem(
                                        label = stringResource(Res.string.nav_login),
                                        isActive = currentRoute == Screen.Login.route,
                                        onClick = {
                                            menuExpanded = false
                                            navController.navigate(Screen.Login.route)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandLabel(onClick: () -> Unit) {
    Text(
        text = "LhanBoyy",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun DesktopNavigationRow(
    currentRoute: String?,
    isAdmin: Boolean,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    windowChrome: AppWindowChrome,
    borderColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            label = stringResource(Res.string.nav_home),
            isActive = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) }
        )
        NavItem(
            label = stringResource(Res.string.nav_posts),
            isActive = currentRoute == Screen.Posts.route,
            onClick = { onNavigate(Screen.Posts.route) }
        )
        NavItem(
            label = stringResource(Res.string.nav_tags),
            isActive = currentRoute == Screen.Tags.route,
            onClick = { onNavigate(Screen.Tags.route) }
        )
        NavItem(
            label = stringResource(Res.string.nav_about),
            isActive = currentRoute == Screen.About.route,
            onClick = { onNavigate(Screen.About.route) }
        )
        if (isAdmin) {
            NavItem(
                label = stringResource(Res.string.nav_admin),
                isActive = currentRoute?.startsWith("admin") == true,
                onClick = { onNavigate(Screen.AdminPostList.route) }
            )
        }

        Spacer(Modifier.width(6.dp))

        HeaderIconButton(
            onClick = { onNavigate(Screen.Search.route) },
            contentDescription = stringResource(Res.string.nav_search)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (currentRoute == Screen.Search.route) AccentColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        ThemeToggleIcon(themeMode = themeMode, onThemeModeSelected = onThemeModeSelected)

        NavItem(
            label = stringResource(if (isLoggedIn) Res.string.nav_logout else Res.string.nav_login),
            isActive = !isLoggedIn && currentRoute == Screen.Login.route,
            onClick = {
                if (isLoggedIn) {
                    onLogout()
                } else {
                    onNavigate(Screen.Login.route)
                }
            }
        )

        val windowActions = windowChrome.windowActions
        if (windowActions != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(borderColor)
            )
            Spacer(Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = windowActions
            )
        }
    }
}

@Composable
private fun HeaderDragSpacer(
    windowChrome: AppWindowChrome,
    modifier: Modifier = Modifier
) {
    val dragArea = windowChrome.dragArea
    if (dragArea != null) {
        dragArea(modifier) {
            Box(Modifier.fillMaxSize())
        }
    } else {
        Spacer(modifier)
    }
}

@Composable
private fun NavItem(label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isActive) AccentColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(2.dp)
                .background(if (isActive) AccentColor else Color.Transparent)
        )
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp).clip(CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ThemeToggleIcon(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit
) {
    val (icon, next) = when (themeMode) {
        ThemeMode.DARK -> Res.drawable.ic_sun to ThemeMode.LIGHT
        ThemeMode.LIGHT -> Res.drawable.ic_monitor to ThemeMode.SYSTEM
        ThemeMode.SYSTEM -> Res.drawable.ic_moon to ThemeMode.DARK
    }
    val tooltipText = when (next) {
        ThemeMode.LIGHT -> "切换到明亮模式"
        ThemeMode.DARK -> "切换到暗黑模式"
        ThemeMode.SYSTEM -> "跟随系统"
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        HeaderIconButton(
            onClick = { onThemeModeSelected(next) },
            contentDescription = tooltipText
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MobileNavItem(label: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isActive) AccentColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp
        )
        if (isActive) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AccentColor)
            )
        }
    }
}
