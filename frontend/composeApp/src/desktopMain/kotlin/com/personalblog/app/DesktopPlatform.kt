package com.personalblog.app

enum class DesktopPlatform {
    MacOs,
    Windows,
    Linux,
    Other
}

fun currentDesktopPlatform(): DesktopPlatform {
    val osName = System.getProperty("os.name").orEmpty().lowercase()
    return when {
        osName.contains("mac") -> DesktopPlatform.MacOs
        osName.contains("win") -> DesktopPlatform.Windows
        osName.contains("nux") || osName.contains("nix") -> DesktopPlatform.Linux
        else -> DesktopPlatform.Other
    }
}
