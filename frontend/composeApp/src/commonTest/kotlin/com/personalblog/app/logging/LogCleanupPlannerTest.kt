package com.personalblog.app.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogCleanupPlannerTest {
    private val config = WebLogStorageConfig(
        maxTotalBytes = 100,
        maxRecordCount = 3,
        softLimitBytes = 80,
        hardLimitBytes = 100,
        cleanupOnWriteThreshold = 2,
        retentionDays = 1,
        alwaysPersistLevels = setOf(LogLevel.ERROR)
    )

    @Test
    fun `decides cleanup for soft limit and write threshold`() {
        val decision = LogCleanupPlanner.decide(
            stats = LogStorageStats(totalBytes = 90, recordCount = 2, writesSinceCleanup = 2),
            config = config
        )

        assertTrue(decision.shouldCleanup)
        assertTrue(CleanupReason.SOFT_LIMIT in decision.reasons)
        assertTrue(CleanupReason.WRITE_THRESHOLD in decision.reasons)
    }

    @Test
    fun `updates metadata after append and delete`() {
        val appended = LogCleanupPlanner.updateAfterAppend(LogStorageStats(), 24)
        assertEquals(24, appended.totalBytes)
        assertEquals(1, appended.recordCount)

        val deleted = LogCleanupPlanner.updateAfterDelete(appended, deletedBytes = 10, deletedCount = 1, cleanupAtEpochMs = 42)
        assertEquals(14, deleted.totalBytes)
        assertEquals(0, deleted.recordCount)
        assertEquals(42, deleted.lastCleanupAtEpochMs)
        assertEquals(0, deleted.writesSinceCleanup)
    }

    @Test
    fun `deletes oldest low priority records first`() {
        val deletions = LogCleanupPlanner.chooseRecordsToDelete(
            records = listOf(
                StoredLogSummary("1", LogLevel.DEBUG, 1, 40),
                StoredLogSummary("2", LogLevel.INFO, 2, 40),
                StoredLogSummary("3", LogLevel.ERROR, 3, 40)
            ),
            config = config,
            nowEpochMs = 4
        )

        assertEquals(listOf("1"), deletions)
    }
}
