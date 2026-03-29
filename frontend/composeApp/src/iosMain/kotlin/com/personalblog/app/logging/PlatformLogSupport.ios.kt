package com.personalblog.app.logging

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.S_IFDIR
import platform.posix.closedir
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.mkdir
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.remove
import platform.posix.stat

@OptIn(ExperimentalForeignApi::class)
actual object PlatformLogSupport {
    actual val platformId: String = "ios"

    actual fun isDebugBuild(): Boolean = true

    actual fun currentThreadName(): String? = null

    actual fun nativeLogDirectory(appName: String): String? {
        val base = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true).firstOrNull() as? String
        return base?.let { "$it/logs" }
    }

    actual suspend fun ensureDirectory(path: String) {
        createDirectories(path)
    }

    actual suspend fun appendLine(path: String, line: String) {
        createDirectories(path.substringBeforeLast('/'))
        fopen(path, "a")?.let { file ->
            fputs(line + "\n", file)
            fclose(file)
        }
    }

    actual suspend fun readDirectory(path: String): List<PlatformFileEntry> {
        val dir = opendir(path) ?: return emptyList()
        val entries = mutableListOf<PlatformFileEntry>()
        try {
            while (true) {
                val entry = readdir(dir) ?: break
                val name = entry.pointed.d_name.toKString()
                if (name == "." || name == "..") continue
                val fullPath = "$path/$name"
                memScoped {
                    val statBuf = alloc<stat>()
                    if (platform.posix.stat(fullPath, statBuf.ptr) == 0) {
                        entries += PlatformFileEntry(
                            path = fullPath,
                            name = name,
                            sizeBytes = statBuf.st_size,
                            lastModifiedEpochMs = statBuf.st_mtimespec.tv_sec * 1000L
                        )
                    }
                }
            }
        } finally {
            closedir(dir)
        }
        return entries
    }

    actual suspend fun deleteFile(path: String) {
        remove(path)
    }

    actual fun joinPath(base: String, child: String): String = "$base/$child"

    actual fun printToConsole(level: LogLevel, message: String) {
        NSLog("%s", message)
    }

    actual fun createPlatformPersistenceSink(config: LogConfig): LogSink? =
        if (config.file.enabled) RollingFileSink(config) else null
}

@OptIn(ExperimentalForeignApi::class)
private fun createDirectories(path: String) {
    if (path.isBlank()) return
    var current = if (path.startsWith("/")) "/" else ""
    path.split('/').filter { it.isNotBlank() }.forEach { segment ->
        current = if (current == "/" || current.isEmpty()) "$current$segment" else "$current/$segment"
        mkdir(current, 511u)
    }
}
