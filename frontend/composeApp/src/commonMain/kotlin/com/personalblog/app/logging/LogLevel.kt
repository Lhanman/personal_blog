package com.personalblog.app.logging

import kotlinx.serialization.Serializable

@Serializable
enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    fun allows(other: LogLevel): Boolean = other.ordinal >= ordinal
}
