package com.personalblog.app.logging

import kotlinx.datetime.Clock


data class LogStorageStats(
    val totalBytes: Long = 0,
    val recordCount: Int = 0,
    val lastCleanupAtEpochMs: Long = 0,
    val writesSinceCleanup: Int = 0
)

data class StoredLogSummary(
    val id: String,
    val level: LogLevel,
    val timestampMs: Long,
    val approxBytes: Int
)

enum class CleanupReason {
    STARTUP,
    SOFT_LIMIT,
    HARD_LIMIT,
    RECORD_LIMIT,
    RETENTION,
    WRITE_THRESHOLD
}

data class CleanupDecision(
    val shouldCleanup: Boolean,
    val reasons: Set<CleanupReason>
)

object LogCleanupPlanner {
    fun decide(
        stats: LogStorageStats,
        config: WebLogStorageConfig,
        startup: Boolean = false,
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()
    ): CleanupDecision {
        val reasons = mutableSetOf<CleanupReason>()
        if (startup && config.cleanupOnStartup) reasons += CleanupReason.STARTUP
        if (stats.totalBytes >= config.hardLimitBytes) reasons += CleanupReason.HARD_LIMIT
        if (stats.totalBytes >= config.softLimitBytes) reasons += CleanupReason.SOFT_LIMIT
        if (stats.recordCount >= config.maxRecordCount) reasons += CleanupReason.RECORD_LIMIT
        if (stats.writesSinceCleanup >= config.cleanupOnWriteThreshold) reasons += CleanupReason.WRITE_THRESHOLD
        if (stats.lastCleanupAtEpochMs > 0 && nowEpochMs - stats.lastCleanupAtEpochMs >= config.retentionDays * 24L * 60 * 60 * 1000) {
            reasons += CleanupReason.RETENTION
        }
        return CleanupDecision(shouldCleanup = reasons.isNotEmpty(), reasons = reasons)
    }

    fun updateAfterAppend(stats: LogStorageStats, recordBytes: Int): LogStorageStats = stats.copy(
        totalBytes = stats.totalBytes + recordBytes,
        recordCount = stats.recordCount + 1,
        writesSinceCleanup = stats.writesSinceCleanup + 1
    )

    fun updateAfterDelete(
        stats: LogStorageStats,
        deletedBytes: Long,
        deletedCount: Int,
        cleanupAtEpochMs: Long = Clock.System.now().toEpochMilliseconds()
    ): LogStorageStats = stats.copy(
        totalBytes = (stats.totalBytes - deletedBytes).coerceAtLeast(0),
        recordCount = (stats.recordCount - deletedCount).coerceAtLeast(0),
        lastCleanupAtEpochMs = cleanupAtEpochMs,
        writesSinceCleanup = 0
    )

    fun chooseRecordsToDelete(
        records: List<StoredLogSummary>,
        config: WebLogStorageConfig,
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()
    ): List<String> {
        if (records.isEmpty()) return emptyList()

        val retentionCutoff = nowEpochMs - config.retentionDays * 24L * 60 * 60 * 1000
        val deletions = mutableListOf<String>()
        val mutable = records.sortedWith(compareBy<StoredLogSummary>({ it.timestampMs }, { it.level.ordinal })).toMutableList()

        val expired = mutable.filter { it.timestampMs < retentionCutoff }
        deletions += expired.map { it.id }
        mutable.removeAll(expired.toSet())

        fun isProtected(record: StoredLogSummary): Boolean = record.level in config.alwaysPersistLevels

        fun currentBytes(): Long = mutable.sumOf { it.approxBytes.toLong() }

        while (mutable.size > config.maxRecordCount || currentBytes() > config.softLimitBytes) {
            val candidateIndex = mutable.indexOfFirst { !isProtected(it) }
                .takeIf { it >= 0 }
                ?: mutable.indexOfFirst { it.level != LogLevel.ERROR }
                    .takeIf { it >= 0 }
                ?: mutable.indices.firstOrNull()
                ?: break
            deletions += mutable.removeAt(candidateIndex).id
        }

        return deletions
    }
}
