## Context

当前项目的前端日志能力分散且薄弱：`commonMain` 仅通过 Ktor Client `Logging` 插件输出网络日志，Desktop/JVM 端还依赖运行时提供的 SLF4J provider；业务侧没有统一 `Logger` 抽象，也没有统一的日志字段、目录规范、轮转策略与敏感信息脱敏。由于项目已支持 Android、Desktop、iOS、Web，多端继续各自输出日志会提高排障成本，并放大性能与一致性风险。Web 端还需要在浏览器约束下提供真正可持久化、可清理、可配置的本地日志方案。

本次变更限定在 `:composeApp`，目标是在不破坏现有 MVVM 结构的前提下，为前端建立一套统一日志基础设施，并优先覆盖网络层与关键业务流程。

## Goals / Non-Goals

**Goals:**
- 提供统一的 `AppLogger` API，支持 `TRACE/DEBUG/INFO/WARN/ERROR`。
- 定义统一 `LogEvent` 与 `LogFormatter`，输出稳定、可读、可解析的单行结构化文本。
- 建立异步分发管线，避免主线程直接执行文件 IO。
- 通过 `expect/actual` 适配 Android、Desktop、iOS 的目录与写入实现，并为 Web 提供基于 IndexedDB 的持久化存储。
- 将 `ApiClient` 的 Ktor 网络日志统一桥接到新日志系统。
- 支持基本轮转、保留策略与敏感信息脱敏。
- 支持配置化治理，包括总容量、条数、单条大小、保留天数、软硬阈值、清理触发时机、级别保留策略与脱敏配置。

**Non-Goals:**
- 不改造后端日志系统。
- 不在第一阶段引入远程日志上报服务。
- 不覆盖所有 UI 组件的细粒度埋点。
- 不依赖浏览器真实文件系统 API 作为 Web 默认日志方案。

## Decisions

### Decision 1：采用 `commonMain` 统一日志核心 + 各平台 `expect/actual` sink
- 决策内容：在 `commonMain` 中定义 `AppLogger`、`LogEvent`、`LogConfig`、`LogFormatter`、`AsyncLogDispatcher`、`LogSink`；平台相关目录解析与文件写入通过 `expect/actual` 实现。
- 理由：日志字段、格式、级别与分发策略应在多端共享，只有目录与文件 API 需要平台差异化。这种方式最符合 KMP 架构，也方便测试。
- 替代方案：
  - 各平台分别实现日志系统：一致性差，维护成本高。
  - 直接引入单一第三方跨平台日志库：接入快，但文件落盘、格式、脱敏和轮转的可控性不足。

### Decision 2：统一日志格式采用单行结构化文本
- 决策内容：默认格式为单行 `key=value` 结构，如 `ts=... level=INFO platform=desktop tag=ApiClient msg="..."`，异常与扩展字段按约定字段名输出。
- 理由：比自由文本更规范，便于 grep 与后续接入外部日志系统；比完整 JSON 更易人工阅读，也更容易控制开销。
- 替代方案：
  - 纯文本拼接：不利于统一和解析。
  - JSON Lines：结构更强，但移动端与本地排障场景下可读性较差。

### Decision 3：文件落盘采用单消费者异步写入模型
- 决策内容：业务线程仅提交 `LogEvent` 到有界缓冲，后台协程单线程批量格式化与写文件，按时间或条数触发 flush。
- 理由：可降低锁竞争，避免频繁 open/close 和主线程阻塞，适合前端多协程场景。
- 替代方案：
  - 每条日志同步写文件：实现简单，但性能差。
  - 多线程并发写一个文件：顺序难控制，文件锁复杂。

### Decision 4：目录与轮转策略按平台约定实现
- 决策内容：Android 写入 `filesDir/logs`；Desktop 按 OS 写入用户应用数据目录；iOS 写入应用沙盒；Web 默认写入 IndexedDB。原生平台默认按天滚动，并结合大小阈值与保留天数清理旧日志；Web 使用 IndexedDB object store 保存日志记录与元数据，通过容量/条数/保留期驱动清理。
- 理由：符合各平台存储约定，便于用户定位与调试；Web 使用 IndexedDB 比 `localStorage` 更适合异步、大容量、结构化存储。
- 替代方案：
  - 所有平台统一相对路径：不符合系统习惯，也不可靠。
  - Web 使用 `localStorage`：同步 API 性能差，容量和结构化能力不足。
  - 仅控制台输出：无法满足问题排查与历史追踪需求。

### Decision 5：Web 端采用 IndexedDB + 元数据表管理清理策略
- 决策内容：Web 端日志 sink 以 IndexedDB 为主存储，至少包含 `logs` 与 `meta` 两类 store。`logs` 保存结构化日志记录与估算大小，`meta` 保存 `totalBytes`、`recordCount`、`lastCleanupAt`、schema version 等聚合元信息，用于避免每次写入全量扫描。
- 理由：IndexedDB 具备异步与结构化数据优势；配套元数据能显著降低清理和容量判断成本。
- 替代方案：
  - 每次写入后全量统计 IndexedDB：实现简单，但性能不可控。
  - 仅按条数上限清理：无法准确控制空间占用。

### Decision 6：网络日志桥接到统一 `AppLogger`
- 决策内容：保留 Ktor `Logging` 插件，但通过自定义 logger 桥接到 `AppLogger`，并对 header/body 执行脱敏和级别控制。
- 理由：复用现有网络栈，变更小，同时让网络日志与业务日志具备统一格式与策略。
- 替代方案：
  - 保持 Ktor 默认日志输出：无法统一格式和输出目标。
  - 自行完全重写请求日志：成本更高，重复造轮子。

### Decision 7：日志治理能力以配置对象统一管理
- 决策内容：在全平台 `LogConfig` 之上增加平台专项配置，其中 Web 至少包含 `minPersistLevel`、`maxTotalBytes`、`maxRecordCount`、`maxPerEntryBytes`、`retentionDays`、`softLimitBytes`、`hardLimitBytes`、`cleanupOnStartup`、`cleanupOnWriteThreshold`、`alwaysPersistLevels`、`redactKeys`、`maxFieldLength`、`maxStacktraceLength` 等配置项。
- 理由：日志系统治理需求变化频繁，配置化比硬编码更适合后续调优，也方便区分 Debug/Release。
- 替代方案：
  - 将所有阈值写死在 sink 内部：后期调试成本高。
  - 完全运行时自由配置：实现复杂度高，第一阶段没有必要。

## Risks / Trade-offs

- [日志过量导致性能波动] → 通过级别过滤、有界缓冲、批量 flush 与 body 默认关闭控制开销。
- [文件落盘实现跨平台差异较大] → 目录解析与写入能力通过 `expect/actual` 封装，先覆盖 Android/Desktop/iOS，Web 采用降级策略。
- [Web 端 IndexedDB 清理过于频繁] → 使用 `softLimit`/`hardLimit` 双阈值、写入计数阈值与元数据缓存降低清理频率。
- [IndexedDB 空间估算不精确] → 使用格式化后文本长度作为近似值，并在清理时按保守策略处理。
- [敏感信息泄露] → 在 formatter 或 network bridge 中统一做 header/body 脱敏，默认不打印 token/password。
- [崩溃前日志丢失] → 对 `WARN/ERROR` 提高 flush 优先级，并在应用关闭时执行最终 flush。
- [复杂度上升] → 第一阶段先做最小可用能力：统一 API、格式、异步写入、基础轮转、网络桥接。

## Migration Plan

1. 新增 `commonMain` 日志核心与平台 `expect/actual` 文件能力，不替换现有业务代码。
2. 接入应用初始化流程，在 Android/Desktop/iOS/Web 启动时初始化 logger。
3. 为 Web 增加 IndexedDB schema、元数据维护与配置化清理逻辑。
4. 将 `ApiClient` 的 Ktor `Logging` 插件接到 `AppLogger`。
5. 补充关键 ViewModel 与启动流程日志，逐步替换零散输出。
6. 通过编译、单元测试与平台运行验证后，再根据需要扩展更多业务埋点。
7. 回滚时可保留日志 API，但关闭 `FileSink`、IndexedDB sink 与高频日志桥接，退回控制台输出。

## Open Questions

- iOS 日志目录优先使用 `Documents/logs` 还是 `Library/Caches/logs`？
- 是否需要在第一阶段就引入压缩归档与上传能力？
- Web 端第一版是否要暴露日志导出能力，还是先只做持久化与清理？
