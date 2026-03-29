package com.personalblog.app.logging

interface LogSink {
    val name: String
    val minLevel: LogLevel

    suspend fun write(record: FormattedLogRecord)

    suspend fun writeBatch(records: List<FormattedLogRecord>) {
        for (record in records) {
            write(record)
        }
    }

    suspend fun flush() {}

    suspend fun close() {}
}

class ConsoleSink(
    override val minLevel: LogLevel
) : LogSink {
    override val name: String = "console"

    override suspend fun write(record: FormattedLogRecord) {
        PlatformLogSupport.printToConsole(record.event.level, record.formatted)
    }
}
