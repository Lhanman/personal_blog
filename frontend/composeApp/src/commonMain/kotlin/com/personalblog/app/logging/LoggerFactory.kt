package com.personalblog.app.logging

object LoggerFactory {
    fun getLogger(tag: String): AppLogger {
        LoggingRuntime.ensureInitialized()
        return AppLogger(tag)
    }
}
