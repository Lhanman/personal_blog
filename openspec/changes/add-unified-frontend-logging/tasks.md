## 1. 日志核心模型

- [x] 1.1 在 `commonMain` 新增 `logging/` 目录与 `LogLevel`、`LogEvent`、`LogConfig`、`AppLogger` 基础类型
- [x] 1.2 在 `commonMain` 设计全平台与 Web 专项日志配置对象，覆盖容量、条数、单条大小、保留期、阈值、保留级别与脱敏字段
- [x] 1.3 实现统一 `LogFormatter`，固化字段顺序、异常输出、字段裁剪与敏感信息脱敏规则
- [x] 1.4 实现 `ConsoleSink`、`LogSink` 抽象与 `LoggerFactory`

## 2. 异步写入与轮转

- [x] 2.1 实现 `AsyncLogDispatcher`，支持有界缓冲、批量写入、flush 与 shutdown
- [x] 2.2 实现原生平台基础文件轮转与清理策略，覆盖按日期/大小滚动与历史日志清理
- [x] 2.3 为 `WARN`/`ERROR` 增加高优先级 flush 策略并补充对应测试
- [x] 2.4 实现通用清理决策逻辑，支持软硬阈值、保留期、条数上限与保留级别策略

## 3. 多端文件 sink 适配

- [x] 3.1 通过 `expect/actual` 实现 Android 平台日志目录解析与文件写入
- [x] 3.2 通过 `expect/actual` 实现 Desktop 平台日志目录解析与文件写入
- [x] 3.3 通过 `expect/actual` 实现 iOS 平台日志目录解析与文件写入
- [x] 3.4 为 Web 平台设计 IndexedDB schema 与元数据 store，保存日志记录、估算大小与聚合统计
- [x] 3.5 为 Web 平台实现 `IndexedDbSink`、清理流程与 console 降级策略

## 4. 网络与业务接入

- [x] 4.1 将 `ApiClient` 的 Ktor `Logging` 插件桥接到统一 `AppLogger`
- [x] 4.2 在 Android/Desktop/iOS/Web 启动入口初始化日志系统与环境配置
- [x] 4.3 为 Web 启动流程接入 `cleanupOnStartup`、写入阈值清理与 IndexedDB 降级检测
- [x] 4.4 为关键 ViewModel、启动流程和异常路径接入统一日志 API

## 5. 配置、文档与验证

- [x] 5.1 增加 Debug/Release 差异化日志策略与可配置项
- [x] 5.2 补充 Web IndexedDB 日志存储、清理策略与配置项文档
- [x] 5.3 为 formatter、脱敏、清理决策与 IndexedDB 元数据维护补充测试
- [x] 5.4 运行 `./gradlew :composeApp:compileKotlinDesktop`、`./gradlew :composeApp:wasmJsBrowserDistribution` 与相关测试验证日志系统可编译、可运行
