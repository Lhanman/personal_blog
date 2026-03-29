package com.personalblog.app.logging

class LogFormatter(
    private val config: LogConfig
) {
    private val redactKeys = config.redactKeys.map { it.lowercase() }.toSet()

    fun format(event: LogEvent): FormattedLogRecord {
        val sanitizedMessage = sanitizeFreeText(event.message, config.maxFieldLength)
        val sanitizedExtras = event.extras.mapValues { (key, value) -> sanitizeField(key, value, config.maxFieldLength) }
        val exceptionType = event.throwable?.let { it::class.simpleName ?: it::class.toString() }
        val exceptionMessage = event.throwable?.message?.let { sanitizeFreeText(it, config.maxFieldLength) }
        val stackTrace = event.throwable?.stackTraceToString()?.let { sanitizeFreeText(it, config.maxStacktraceLength) }
        val parts = buildList {
            add("ts=${event.timestamp}")
            add("level=${event.level.name}")
            add("platform=${event.platform}")
            add("tag=${quote(event.tag)}")
            event.feature?.let { add("feature=${quote(it)}") }
            add("sessionId=${quote(event.sessionId)}")
            event.traceId?.let { add("traceId=${quote(it)}") }
            event.threadName?.let { add("thread=${quote(it)}") }
            add("msg=${quote(sanitizedMessage)}")
            if (exceptionType != null) add("error=${quote(exceptionType)}")
            if (exceptionMessage != null) add("errorMessage=${quote(exceptionMessage)}")
            sanitizedExtras.entries
                .sortedBy { it.key }
                .forEach { entry ->
                    add("extra.${normalizeKey(entry.key)}=${quote(entry.value)}")
                }
        }
        val line = parts.joinToString(separator = " ")
        return FormattedLogRecord(
            event = event,
            formatted = line,
            approxBytes = line.encodeToByteArray().size,
            exceptionType = exceptionType,
            exceptionMessage = exceptionMessage,
            stackTrace = stackTrace
        )
    }

    fun sanitizeField(key: String, value: String, maxLength: Int): String {
        val loweredKey = key.lowercase()
        if (loweredKey in redactKeys) return "***"
        return sanitizeFreeText(value, maxLength)
    }

    fun sanitizeFreeText(text: String, maxLength: Int): String {
        val redacted = redactInlineSensitiveData(text)
        val normalized = redacted
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t")
        return if (normalized.length <= maxLength) normalized else normalized.take(maxLength) + "…"
    }

    private fun redactInlineSensitiveData(text: String): String {
        var result = text
        result = result.replace(
            Regex("(?i)(authorization\\s*[=:]\\s*Bearer\\s+)([^,;\\s]+)"),
            "$1***"
        )
        redactKeys.forEach { key ->
            val regex = Regex("(?i)($key)(\\s*[=:]\\s*)([^,;\\s]+)")
            result = result.replace(regex, "$1$2***")
        }
        return result
    }

    private fun normalizeKey(key: String): String = key.replace(' ', '_')

    private fun quote(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}
