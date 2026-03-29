package com.personalblog.app.logging

import java.io.File

actual object PlatformLogSupport {
    actual val platformId: String = "desktop"

    actual fun isDebugBuild(): Boolean = System.getProperty("personalblog.debug")?.toBooleanStrictOrNull() ?: true

    actual fun currentThreadName(): String? = Thread.currentThread().name

    actual fun nativeLogDirectory(appName: String): String? {
        val home = System.getProperty("user.home") ?: return null
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        return when {
            osName.contains("mac") -> File(home, "Library/Application Support/$appName/logs").absolutePath
            osName.contains("win") -> {
                val appData = System.getenv("APPDATA") ?: File(home, "AppData/Roaming").absolutePath
                File(appData, "$appName/logs").absolutePath
            }
            else -> File(home, ".local/share/${appName.lowercase()}/logs").absolutePath
        }
    }

    actual suspend fun ensureDirectory(path: String) {
        File(path).mkdirs()
    }

    actual suspend fun appendLine(path: String, line: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.appendText(line + "\n")
    }

    actual suspend fun readDirectory(path: String): List<PlatformFileEntry> =
        File(path).listFiles().orEmpty().map {
            PlatformFileEntry(
                path = it.absolutePath,
                name = it.name,
                sizeBytes = it.length(),
                lastModifiedEpochMs = it.lastModified()
            )
        }

    actual suspend fun deleteFile(path: String) {
        File(path).delete()
    }

    actual fun joinPath(base: String, child: String): String = File(base, child).absolutePath

    actual fun printToConsole(level: LogLevel, message: String) {
        println(message)
    }

    actual fun createPlatformPersistenceSink(config: LogConfig): LogSink? =
        if (config.file.enabled) RollingFileSink(config) else null
}
