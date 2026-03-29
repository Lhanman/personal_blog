package com.personalblog.app.logging

import android.content.Context
import android.util.Log
import com.personalblog.app.BuildConfig
import java.io.File

private var applicationContext: Context? = null

fun provideAndroidLogContext(context: Context) {
    applicationContext = context.applicationContext
}

actual object PlatformLogSupport {
    actual val platformId: String = "android"

    actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG

    actual fun currentThreadName(): String? = Thread.currentThread().name

    actual fun nativeLogDirectory(appName: String): String? = applicationContext?.filesDir?.resolve("logs")?.absolutePath

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
        when (level) {
            LogLevel.TRACE -> Log.v("PersonalBlog", message)
            LogLevel.DEBUG -> Log.d("PersonalBlog", message)
            LogLevel.INFO -> Log.i("PersonalBlog", message)
            LogLevel.WARN -> Log.w("PersonalBlog", message)
            LogLevel.ERROR -> Log.e("PersonalBlog", message)
        }
    }

    actual fun createPlatformPersistenceSink(config: LogConfig): LogSink? =
        if (config.file.enabled && nativeLogDirectory(config.appName) != null) RollingFileSink(config) else null
}
