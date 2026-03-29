package com.personalblog.app.logging

data class PlatformFileEntry(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val lastModifiedEpochMs: Long
)

expect object PlatformLogSupport {
    val platformId: String

    fun isDebugBuild(): Boolean

    fun currentThreadName(): String?

    fun nativeLogDirectory(appName: String): String?

    suspend fun ensureDirectory(path: String)

    suspend fun appendLine(path: String, line: String)

    suspend fun readDirectory(path: String): List<PlatformFileEntry>

    suspend fun deleteFile(path: String)

    fun joinPath(base: String, child: String): String

    fun printToConsole(level: LogLevel, message: String)

    fun createPlatformPersistenceSink(config: LogConfig): LogSink?
}

class RollingFileSink(
    private val config: LogConfig,
    override val minLevel: LogLevel = config.file.minLevel
) : LogSink {
    override val name: String = "native-file"
    private val directory: String = requireNotNull(PlatformLogSupport.nativeLogDirectory(config.appName))
    private val filePrefix: String = config.file.filePrefix

    override suspend fun write(record: FormattedLogRecord) {
        PlatformLogSupport.ensureDirectory(directory)
        val targetFile = resolveCurrentFile(record)
        PlatformLogSupport.appendLine(targetFile, record.formatted)
        cleanupOldFiles()
    }

    private suspend fun resolveCurrentFile(record: FormattedLogRecord): String {
        val day = record.event.timestamp.toString().substringBefore('T')
        val baseName = "$filePrefix-$day"
        val candidates = PlatformLogSupport.readDirectory(directory)
            .filter { it.name.startsWith(baseName) && it.name.endsWith(".log") }
            .sortedBy { extractIndex(baseName, it.name) }
        val latest = candidates.lastOrNull()
        if (latest == null) return PlatformLogSupport.joinPath(directory, "$baseName.log")
        return if (latest.sizeBytes < config.file.maxFileBytes) {
            latest.path
        } else {
            PlatformLogSupport.joinPath(directory, "$baseName-${extractIndex(baseName, latest.name) + 1}.log")
        }
    }

    private suspend fun cleanupOldFiles() {
        val files = PlatformLogSupport.readDirectory(directory)
            .filter { it.name.startsWith(filePrefix) && it.name.endsWith(".log") }
            .sortedByDescending { it.lastModifiedEpochMs }
        val overflow = files.drop(config.file.maxRetainedFiles)
        overflow.forEach { PlatformLogSupport.deleteFile(it.path) }
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        files.filter { now - it.lastModifiedEpochMs > config.file.retentionDays * 24L * 60 * 60 * 1000 }
            .forEach { PlatformLogSupport.deleteFile(it.path) }
    }

    private fun extractIndex(baseName: String, fileName: String): Int {
        if (fileName == "$baseName.log") return 0
        val suffix = fileName.removePrefix("$baseName-").removeSuffix(".log")
        return suffix.toIntOrNull() ?: 0
    }
}

object LoggingRuntime {
    private var dispatcher: AsyncLogDispatcher? = null

    var currentConfig: LogConfig = LogConfig()
        private set

    fun ensureInitialized() {
        if (dispatcher == null) {
            initialize(currentConfig)
        }
    }

    fun initialize(config: LogConfig = LogConfig()) {
        dispatcher?.closeAsync()
        currentConfig = config
        val formatter = LogFormatter(config)
        val sinks = buildList {
            if (config.console.enabled) add(ConsoleSink(config.console.minLevel))
            PlatformLogSupport.createPlatformPersistenceSink(config)?.let(::add)
        }
        dispatcher = AsyncLogDispatcher(config, formatter, sinks)
        LoggerFactory.getLogger("LoggingRuntime").info(
            message = "logging initialized",
            feature = "logging",
            extras = mapOf(
                "platform" to PlatformLogSupport.platformId,
                "consoleEnabled" to config.console.enabled.toString(),
                "fileEnabled" to config.file.enabled.toString(),
                "webEnabled" to config.web.enabled.toString()
            )
        )
    }

    fun dispatch(event: LogEvent) {
        ensureInitialized()
        dispatcher?.submit(event)
    }

    suspend fun flush() {
        dispatcher?.flush()
    }

    suspend fun shutdown() {
        dispatcher?.close()
        dispatcher = null
    }
}
