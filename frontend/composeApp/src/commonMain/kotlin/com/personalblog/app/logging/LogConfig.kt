package com.personalblog.app.logging

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

private const val DEFAULT_TOTAL_BYTES = 5L * 1024 * 1024
private const val DEFAULT_SOFT_LIMIT_BYTES = 4L * 1024 * 1024
private const val DEFAULT_HARD_LIMIT_BYTES = 5L * 1024 * 1024

@Serializable
data class DispatcherConfig(
    val channelCapacity: Int = 1024,
    val batchSize: Int = 20,
    val flushIntervalMs: Long = 500,
    val flushOnLevels: Set<LogLevel> = setOf(LogLevel.WARN, LogLevel.ERROR)
)

@Serializable
data class ConsoleLoggingConfig(
    val enabled: Boolean = true,
    val minLevel: LogLevel = LogLevel.DEBUG
)

@Serializable
data class FileLoggingConfig(
    val enabled: Boolean = true,
    val minLevel: LogLevel = LogLevel.INFO,
    val maxFileBytes: Long = 2L * 1024 * 1024,
    val retentionDays: Int = 7,
    val maxRetainedFiles: Int = 7,
    val filePrefix: String = "app"
)

@Serializable
data class WebLogStorageConfig(
    val enabled: Boolean = true,
    val minPersistLevel: LogLevel = LogLevel.WARN,
    val maxTotalBytes: Long = DEFAULT_TOTAL_BYTES,
    val maxRecordCount: Int = 5000,
    val maxPerEntryBytes: Int = 16 * 1024,
    val retentionDays: Int = 7,
    val softLimitBytes: Long = DEFAULT_SOFT_LIMIT_BYTES,
    val hardLimitBytes: Long = DEFAULT_HARD_LIMIT_BYTES,
    val cleanupOnStartup: Boolean = true,
    val cleanupOnWriteThreshold: Int = 100,
    val alwaysPersistLevels: Set<LogLevel> = setOf(LogLevel.WARN, LogLevel.ERROR),
    val redactKeys: Set<String> = defaultRedactKeys,
    val maxFieldLength: Int = 2048,
    val maxStacktraceLength: Int = 8192
)

@Serializable
data class LogConfig(
    val appName: String = "PersonalBlog",
    val enabled: Boolean = true,
    val minLevel: LogLevel = if (PlatformLogSupport.isDebugBuild()) LogLevel.DEBUG else LogLevel.INFO,
    val sessionId: String = buildDefaultSessionId(),
    val dispatcher: DispatcherConfig = DispatcherConfig(),
    val console: ConsoleLoggingConfig = ConsoleLoggingConfig(
        minLevel = if (PlatformLogSupport.isDebugBuild()) LogLevel.DEBUG else LogLevel.INFO
    ),
    val file: FileLoggingConfig = FileLoggingConfig(),
    val web: WebLogStorageConfig = WebLogStorageConfig(
        minPersistLevel = if (PlatformLogSupport.isDebugBuild()) LogLevel.INFO else LogLevel.WARN
    ),
    val redactKeys: Set<String> = defaultRedactKeys,
    val maxFieldLength: Int = 2048,
    val maxStacktraceLength: Int = 8192
)

val defaultRedactKeys: Set<String> = setOf(
    "token",
    "authorization",
    "password",
    "cookie",
    "set-cookie",
    "jwt",
    "secret"
)

fun buildDefaultSessionId(nowMillis: Long = Clock.System.now().toEpochMilliseconds()): String =
    "${PlatformLogSupport.platformId}-$nowMillis"
