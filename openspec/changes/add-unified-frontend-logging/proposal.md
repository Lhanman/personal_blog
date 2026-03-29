## Why

当前前端仅在 `ApiClient` 中接入了 Ktor `Logging` 插件，缺少统一的日志抽象、统一格式、统一级别规范与跨平台文件落盘能力。随着 Desktop 支持已接入、平台增多，继续依赖各端默认日志实现会导致排障困难、格式不一致、无法稳定保存日志，也难以控制性能与敏感信息输出。

## What Changes

- 新增一套前端统一日志系统，覆盖 Android、Desktop、iOS 与 Web，其中 Web 端使用 IndexedDB 持久化日志。
- 定义统一日志模型、统一格式化规则、统一级别规范与脱敏策略。
- 新增异步日志分发与落盘能力，支持平台目录适配、Web IndexedDB 存储、轮转与清理。
- 将 Ktor Client 网络日志桥接到统一日志系统，并逐步接入关键业务日志。
- 为 Debug/Release 提供不同日志策略，并支持日志容量、条数、保留期、单条大小、清理触发阈值、级别保留策略等配置项。

## Capabilities

### New Capabilities
- `frontend-logging`: 定义多端统一日志 API、格式、异步写入、文件落盘与网络日志接入规范。

### Modified Capabilities
- 无

## Impact

影响范围主要在 `:composeApp`，涉及 `commonMain` 日志基础设施、各平台 `expect/actual` 文件路径与写入实现、Web IndexedDB schema 与清理策略、`ApiClient` 网络日志桥接、Desktop/Android/iOS/Web 平台适配，以及相关依赖、测试与开发文档。
