package com.personalblog.app.logging

class AppLogger internal constructor(
    private val tag: String
) {
    fun trace(message: String, feature: String? = null, traceId: String? = null, extras: Map<String, String> = emptyMap()) {
        log(LogLevel.TRACE, message, feature, traceId, extras)
    }

    fun debug(message: String, feature: String? = null, traceId: String? = null, extras: Map<String, String> = emptyMap()) {
        log(LogLevel.DEBUG, message, feature, traceId, extras)
    }

    fun info(message: String, feature: String? = null, traceId: String? = null, extras: Map<String, String> = emptyMap()) {
        log(LogLevel.INFO, message, feature, traceId, extras)
    }

    fun warn(
        message: String,
        feature: String? = null,
        traceId: String? = null,
        extras: Map<String, String> = emptyMap(),
        throwable: Throwable? = null
    ) {
        log(LogLevel.WARN, message, feature, traceId, extras, throwable)
    }

    fun error(
        message: String,
        feature: String? = null,
        traceId: String? = null,
        extras: Map<String, String> = emptyMap(),
        throwable: Throwable? = null
    ) {
        log(LogLevel.ERROR, message, feature, traceId, extras, throwable)
    }

    private fun log(
        level: LogLevel,
        message: String,
        feature: String?,
        traceId: String?,
        extras: Map<String, String>,
        throwable: Throwable? = null
    ) {
        LoggingRuntime.dispatch(
            LogEvent(
                level = level,
                tag = tag,
                feature = feature,
                sessionId = LoggingRuntime.currentConfig.sessionId,
                traceId = traceId,
                message = message,
                throwable = throwable,
                extras = extras
            )
        )
    }
}
