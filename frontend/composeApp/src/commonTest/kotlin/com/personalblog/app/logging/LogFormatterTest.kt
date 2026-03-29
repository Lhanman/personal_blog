package com.personalblog.app.logging

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogFormatterTest {
    private val formatter = LogFormatter(
        LogConfig(
            sessionId = "test-session",
            maxFieldLength = 32,
            maxStacktraceLength = 64,
            redactKeys = setOf("authorization", "password")
        )
    )

    @Test
    fun `formats structured log line`() {
        val record = formatter.format(
            LogEvent(
                timestamp = Instant.parse("2026-04-01T00:00:00Z"),
                level = LogLevel.INFO,
                platform = "desktop",
                tag = "ApiClient",
                feature = "network",
                sessionId = "test-session",
                traceId = "trace-1",
                threadName = "main",
                message = "GET /api/v1/posts success",
                extras = mapOf("status" to "200")
            )
        )

        assertTrue(record.formatted.contains("level=INFO"))
        assertTrue(record.formatted.contains("platform=desktop"))
        assertTrue(record.formatted.contains("tag=\"ApiClient\""))
        assertTrue(record.formatted.contains("feature=\"network\""))
        assertTrue(record.formatted.contains("extra.status=\"200\""))
    }

    @Test
    fun `redacts sensitive keys and inline tokens`() {
        val record = formatter.format(
            LogEvent(
                level = LogLevel.ERROR,
                tag = "Auth",
                sessionId = "test-session",
                message = "authorization=Bearer secret-token password=plain-text",
                extras = mapOf(
                    "authorization" to "Bearer token",
                    "password" to "123456"
                )
            )
        )

        assertFalse(record.formatted.contains("secret-token"))
        assertFalse(record.formatted.contains("plain-text"))
        assertTrue(record.formatted.contains("extra.authorization=\"***\""))
        assertTrue(record.formatted.contains("extra.password=\"***\""))
    }

    @Test
    fun `truncates long fields`() {
        val record = formatter.format(
            LogEvent(
                level = LogLevel.INFO,
                tag = "Formatter",
                sessionId = "test-session",
                message = "abcdefghijklmnopqrstuvwxyz0123456789"
            )
        )

        assertTrue(record.formatted.contains("abcdefghijklmnopqrstuvwxyz012345…"))
    }
}
