## ADDED Requirements

### Requirement: 前端 SHALL 提供统一日志 API
系统 SHALL 在 `:composeApp` 中提供统一的前端日志 API，支持 `TRACE`、`DEBUG`、`INFO`、`WARN`、`ERROR` 五个级别，并允许业务代码与基础设施代码通过统一入口记录日志。

#### Scenario: 业务代码记录统一格式日志
- **WHEN** ViewModel、Repository、RemoteDataSource 或应用启动流程输出日志
- **THEN** 系统通过统一 `AppLogger` 入口记录日志，而不是直接依赖平台默认日志接口

#### Scenario: 网络层记录统一入口日志
- **WHEN** `ApiClient` 记录请求或响应日志
- **THEN** 网络日志通过统一日志 API 输出，并遵循相同的字段与级别规则

### Requirement: 前端 SHALL 输出统一格式日志内容
系统 SHALL 将所有前端日志格式化为统一的结构化文本格式，至少包含时间、级别、平台、tag、消息和扩展字段，并支持异常信息与敏感字段脱敏。

#### Scenario: 普通信息日志格式化
- **WHEN** 系统输出一条 `INFO` 日志
- **THEN** 日志文本包含统一字段顺序与字段命名，且不同平台输出格式一致

#### Scenario: 异常日志格式化
- **WHEN** 系统输出带异常的 `ERROR` 日志
- **THEN** 日志文本包含异常类型、错误消息和可选堆栈信息

#### Scenario: 敏感信息脱敏
- **WHEN** 日志内容包含 token、password、authorization header 或其他敏感字段
- **THEN** 系统对敏感内容执行脱敏后再输出或落盘

### Requirement: 前端 SHALL 支持异步文件写入
系统 SHALL 通过异步管线将日志写入平台本地目录，避免在主线程直接执行文件 IO，并保证同一 sink 的日志写入顺序可预期。

#### Scenario: 主线程提交日志
- **WHEN** UI 线程或主线程记录日志
- **THEN** 日志事件进入异步缓冲队列，主线程不直接阻塞在文件写入上

#### Scenario: 批量刷盘
- **WHEN** 异步缓冲达到批量阈值或 flush 时间窗口
- **THEN** 系统按顺序批量写入日志文件

#### Scenario: 高优先级日志优先处理
- **WHEN** 系统记录 `WARN` 或 `ERROR` 日志
- **THEN** 系统优先确保该日志被尽快 flush 到目标 sink

### Requirement: 前端 SHALL 按平台写入约定存储位置
系统 SHALL 根据不同平台把日志写入约定存储位置，并在目录或存储空间不存在时自动初始化；Web 端 MUST 使用 IndexedDB 持久化日志，并在不可用时提供 console 降级策略。

#### Scenario: Android 写入应用私有目录
- **WHEN** Android 端启用文件日志
- **THEN** 系统将日志写入应用私有目录下的 `logs/` 子目录

#### Scenario: Desktop 写入操作系统应用数据目录
- **WHEN** Desktop 端启用文件日志
- **THEN** 系统根据 macOS、Windows、Linux 选择各自推荐的应用数据目录并写入 `logs/`

#### Scenario: Web 端写入 IndexedDB
- **WHEN** Web 端启用持久化日志
- **THEN** 系统将日志写入 IndexedDB 中定义的日志存储，并维护必要的元数据

#### Scenario: Web 端降级输出
- **WHEN** Web 端 IndexedDB 不可用或初始化失败
- **THEN** 系统至少输出到 console，并记录当前持久化能力已降级

### Requirement: 前端 SHALL 支持日志轮转与清理
系统 SHALL 支持基础日志轮转与清理策略；原生平台至少包含按日期或大小滚动文件，并清理超出保留策略的历史日志；Web 端 MUST 支持按容量、条数、保留期和优先级清理 IndexedDB 中的历史日志。

#### Scenario: 到达轮转条件
- **WHEN** 当前日志文件达到日期切换点或大小阈值
- **THEN** 系统创建新的日志文件并继续写入

#### Scenario: 清理过期日志
- **WHEN** 日志目录中的历史文件超过保留天数或保留数量限制
- **THEN** 系统自动清理超限文件

#### Scenario: Web 端按容量清理日志
- **WHEN** IndexedDB 中日志总容量超过配置的软阈值或硬阈值
- **THEN** 系统根据既定清理策略删除低优先级或过期日志，直到容量回落到允许范围内

#### Scenario: Web 端按条数清理日志
- **WHEN** IndexedDB 中日志条数超过配置上限
- **THEN** 系统优先删除较旧且较低级别的日志记录

### Requirement: 前端 SHALL 支持环境化日志策略
系统 SHALL 支持按运行环境配置最小日志级别、控制台输出、文件落盘与网络日志详细程度，以便 Debug 与 Release 使用不同策略。

#### Scenario: Debug 环境输出更详细日志
- **WHEN** 应用运行在 Debug 环境
- **THEN** 系统允许输出 `DEBUG` 级别日志，并可启用更详细的网络日志

#### Scenario: Release 环境降低日志噪声
- **WHEN** 应用运行在 Release 环境
- **THEN** 系统默认至少过滤高频低价值日志，并限制敏感或高开销日志输出

### Requirement: 前端 SHALL 支持配置化日志治理
系统 SHALL 提供可配置的日志治理参数，至少包括最小持久化级别、总容量上限、最大日志条数、单条日志大小上限、保留天数、软硬清理阈值、清理触发时机、保留级别策略和脱敏字段列表。

#### Scenario: 配置最大总容量
- **WHEN** 应用初始化日志系统时提供 `maxTotalBytes`
- **THEN** 系统使用该值作为日志总容量上限参与清理决策

#### Scenario: 配置单条日志大小上限
- **WHEN** 单条日志格式化后的大小超过 `maxPerEntryBytes`
- **THEN** 系统对该日志进行截断或裁剪后再持久化

#### Scenario: 配置保留级别策略
- **WHEN** 系统在清理超限日志时遇到 `alwaysPersistLevels` 中的级别
- **THEN** 系统优先保留这些级别的日志，并先淘汰较低优先级日志

#### Scenario: 配置脱敏字段列表
- **WHEN** 应用初始化日志系统时提供 `redactKeys`
- **THEN** 系统在格式化网络或业务日志时对这些字段执行脱敏
