package com.personalblog.app.logging

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PersistedLogRecord(
    val id: String,
    val timestampMs: Long,
    val level: String,
    val platform: String,
    val tag: String,
    val feature: String? = null,
    val sessionId: String,
    val traceId: String? = null,
    val threadName: String? = null,
    val message: String,
    val formatted: String,
    val approxBytes: Int,
    val exceptionType: String? = null,
    val exceptionMessage: String? = null,
    val stackTrace: String? = null,
    val extras: Map<String, String> = emptyMap()
)

data class LogEvent(
    val timestamp: Instant = Clock.System.now(),
    val level: LogLevel,
    val platform: String = PlatformLogSupport.platformId,
    val tag: String,
    val feature: String? = null,
    val sessionId: String,
    val traceId: String? = null,
    val threadName: String? = PlatformLogSupport.currentThreadName(),
    val message: String,
    val throwable: Throwable? = null,
    val extras: Map<String, String> = emptyMap()
)

data class FormattedLogRecord(
    val event: LogEvent,
    val formatted: String,
    val approxBytes: Int,
    val exceptionType: String? = null,
    val exceptionMessage: String? = null,
    val stackTrace: String? = null
) {
    fun toPersistedRecord(): PersistedLogRecord = PersistedLogRecord(
        id = "${event.sessionId}-${event.timestamp.toEpochMilliseconds()}-${event.tag}-${event.level}",
        timestampMs = event.timestamp.toEpochMilliseconds(),
        level = event.level.name,
        platform = event.platform,
        tag = event.tag,
        feature = event.feature,
        sessionId = event.sessionId,
        traceId = event.traceId,
        threadName = event.threadName,
        message = event.message,
        formatted = formatted,
        approxBytes = approxBytes,
        exceptionType = exceptionType,
        exceptionMessage = exceptionMessage,
        stackTrace = stackTrace,
        extras = event.extras
    )
}
