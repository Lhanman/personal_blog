package com.personalblog.app.logging

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val webLogJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

actual object PlatformLogSupport {
    actual val platformId: String = "web"

    actual fun isDebugBuild(): Boolean = true

    actual fun currentThreadName(): String? = null

    actual fun nativeLogDirectory(appName: String): String? = null

    actual suspend fun ensureDirectory(path: String) = Unit

    actual suspend fun appendLine(path: String, line: String) = Unit

    actual suspend fun readDirectory(path: String): List<PlatformFileEntry> = emptyList()

    actual suspend fun deleteFile(path: String) = Unit

    actual fun joinPath(base: String, child: String): String = "$base/$child"

    actual fun printToConsole(level: LogLevel, message: String) {
        jsConsoleLog(message)
    }

    actual fun createPlatformPersistenceSink(config: LogConfig): LogSink? =
        if (config.web.enabled) IndexedDbSink(config) else null
}

private class IndexedDbSink(
    private val config: LogConfig
) : LogSink {
    override val name: String = "indexeddb"
    override val minLevel: LogLevel = config.web.minPersistLevel

    init {
        jsInitIndexedDb(webLogJson.encodeToString(config))
    }

    override suspend fun write(record: FormattedLogRecord) {
        jsPersistRecord(
            webLogJson.encodeToString(config),
            webLogJson.encodeToString(record.toPersistedRecord())
        )
    }

    override suspend fun flush() = Unit
}

@JsFun("""
(message) => {
  console.log(message);
}
""")
private external fun jsConsoleLog(message: String)

@JsFun("""
(configJson) => {
  const cfg = JSON.parse(configJson);
  const root = globalThis;
  if (!root.__pbLogStore) {
    root.__pbLogStore = {
      config: cfg,
      dbName: cfg.appName + -logs,
      available: typeof root.indexedDB !== 'undefined',
      openPromise: null,
      bootstrapped: false,
      getMetaDefaults() {
        return { key: 'stats', totalBytes: 0, recordCount: 0, lastCleanupAt: 0, writesSinceCleanup: 0 };
      },
      ensureOpen() {
        if (this.openPromise) return this.openPromise;
        if (!this.available) {
          this.openPromise = Promise.resolve(null);
          return this.openPromise;
        }
        this.openPromise = new Promise((resolve, reject) => {
          const request = root.indexedDB.open(this.dbName, 1);
          request.onupgradeneeded = () => {
            const db = request.result;
            const logs = db.createObjectStore('logs', { keyPath: 'id' });
            logs.createIndex('timestampMs', 'timestampMs');
            logs.createIndex('level', 'level');
            logs.createIndex('sessionId', 'sessionId');
            logs.createIndex('tag', 'tag');
            db.createObjectStore('meta', { keyPath: 'key' });
          };
          request.onsuccess = () => resolve(request.result);
          request.onerror = () => reject(request.error);
        });
        return this.openPromise;
      },
      shouldCleanup(meta) {
        if (!meta) return false;
        return meta.totalBytes >= this.config.web.softLimitBytes ||
          meta.totalBytes >= this.config.web.hardLimitBytes ||
          meta.recordCount >= this.config.web.maxRecordCount ||
          meta.writesSinceCleanup >= this.config.web.cleanupOnWriteThreshold;
      },
      cleanup(reason) {
        return this.ensureOpen().then((db) => {
          if (!db) return;
          const tx = db.transaction(['logs', 'meta'], 'readwrite');
          const logsStore = tx.objectStore('logs');
          const metaStore = tx.objectStore('meta');
          const getAllRequest = logsStore.getAll();
          getAllRequest.onsuccess = () => {
            const records = getAllRequest.result || [];
            const now = Date.now();
            const retentionCutoff = now - this.config.web.retentionDays * 24 * 60 * 60 * 1000;
            records.sort((a, b) => (a.timestampMs - b.timestampMs) || levelRank(a.level) - levelRank(b.level));
            const protectedLevels = new Set(this.config.web.alwaysPersistLevels || []);
            const deletions = [];
            let totalBytes = records.reduce((sum, item) => sum + (item.approxBytes || 0), 0);
            let recordCount = records.length;
            for (const record of records) {
              if (record.timestampMs < retentionCutoff) {
                deletions.push(record.id);
                totalBytes -= record.approxBytes || 0;
                recordCount -= 1;
              }
            }
            const survivors = records.filter((item) => !deletions.includes(item.id));
            while (totalBytes > this.config.web.softLimitBytes || recordCount > this.config.web.maxRecordCount) {
              const candidate = survivors.find((item) => !protectedLevels.has(item.level)) || survivors.find((item) => item.level !== 'ERROR') || survivors[0];
              if (!candidate) break;
              deletions.push(candidate.id);
              totalBytes -= candidate.approxBytes || 0;
              recordCount -= 1;
              const index = survivors.findIndex((item) => item.id === candidate.id);
              if (index >= 0) survivors.splice(index, 1);
            }
            deletions.forEach((id) => logsStore.delete(id));
            metaStore.put({
              key: 'stats',
              totalBytes: Math.max(totalBytes, 0),
              recordCount: Math.max(recordCount, 0),
              lastCleanupAt: Date.now(),
              writesSinceCleanup: 0,
              reason
            });
          };
        }).catch(() => console.warn('[logging] indexedDB cleanup failed'));
      }
    };
  }
  root.__pbLogStore.config = cfg;
  root.__pbLogStore.ensureOpen().then(() => {
    if (cfg.web.cleanupOnStartup) {
      root.__pbLogStore.cleanup('startup');
    }
  }).catch(() => console.warn('[logging] indexedDB unavailable; using console sink only'));

  function levelRank(level) {
    switch (level) {
      case 'TRACE': return 0;
      case 'DEBUG': return 1;
      case 'INFO': return 2;
      case 'WARN': return 3;
      case 'ERROR': return 4;
      default: return 0;
    }
  }
}
""")
private external fun jsInitIndexedDb(configJson: String)

@JsFun("""
(configJson, recordJson) => {
  const cfg = JSON.parse(configJson);
  const record = JSON.parse(recordJson);
  const root = globalThis;
  const store = root.__pbLogStore;
  if (!store || typeof root.indexedDB === 'undefined') {
    console.warn('[logging] indexedDB unavailable; persisting to console only');
    console.log(record.formatted);
    return;
  }
  store.config = cfg;
  store.ensureOpen().then((db) => {
    if (!db) {
      console.log(record.formatted);
      return;
    }
    const tx = db.transaction(['logs', 'meta'], 'readwrite');
    const logsStore = tx.objectStore('logs');
    const metaStore = tx.objectStore('meta');
    const metaRequest = metaStore.get('stats');
    metaRequest.onsuccess = () => {
      const meta = metaRequest.result || store.getMetaDefaults();
      const maxEntryBytes = cfg.web.maxPerEntryBytes;
      if ((record.approxBytes || 0) > maxEntryBytes) {
        record.formatted = (record.formatted || '').slice(0, maxEntryBytes) + '…';
        record.approxBytes = maxEntryBytes;
      }
      logsStore.put(record);
      meta.totalBytes += record.approxBytes || 0;
      meta.recordCount += 1;
      meta.writesSinceCleanup += 1;
      metaStore.put(meta);
      if (store.shouldCleanup(meta)) {
        tx.oncomplete = () => store.cleanup('threshold');
      }
    };
    metaRequest.onerror = () => {
      logsStore.put(record);
      metaStore.put({
        key: 'stats',
        totalBytes: record.approxBytes || 0,
        recordCount: 1,
        lastCleanupAt: 0,
        writesSinceCleanup: 1
      });
      tx.oncomplete = () => store.cleanup('fallback');
    };
  }).catch(() => console.warn('[logging] indexedDB write failed'));
}
""")
private external fun jsPersistRecord(configJson: String, recordJson: String)
