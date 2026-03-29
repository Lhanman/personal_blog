package com.personalblog.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// AstroPaper-inspired minimal color scheme
private val LightPrimary = Color(0xFF1E293B)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1E293B)
private val LightSecondary = Color(0xFF64748B)

private val DarkPrimary = Color(0xFFE2E8F0)
private val DarkOnPrimary = Color(0xFF212737)
private val DarkBackground = Color(0xFF212737)
private val DarkSurface = Color(0xFF2A3142)
private val DarkOnSurface = Color(0xFFE2E8F0)
private val DarkSecondary = Color(0xFF94A3B8)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    secondary = LightSecondary
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    secondary = DarkSecondary
)
