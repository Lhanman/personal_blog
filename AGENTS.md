# Repository Guidelines（仓库贡献指南）

## 项目结构与模块组织
本仓库是 Kotlin Multiplatform 多模块项目，核心分为：
- `backend/`（`:backend`）：Ktor 后端服务，代码在 `backend/src/main/kotlin/com/personalblog/backend`，数据库迁移在 `backend/src/main/resources/db/migration`。
- `frontend/composeApp/`（`:composeApp`）：Compose Multiplatform 前端，按平台拆分 `commonMain`、`androidMain`、`iosMain`、`macosMain`、`wasmJsMain`。
- `shared/`（`:shared`）：前后端共享 DTO，位于 `shared/src/commonMain/kotlin/com/personalblog/shared/dto`。

`docker/` 与 `docker-compose.yml` 用于部署；`build/` 目录均为构建产物，不应手改。

## 构建、测试与开发命令
在仓库根目录使用 Gradle Wrapper：
- `./gradlew :backend:run`：启动后端本地开发。
- `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`：启动 Web/WASM 开发模式。
- `./gradlew :composeApp:wasmJsBrowserDistribution`：构建 Web 生产包。
- `./gradlew :composeApp:run`：运行桌面端。
- `./gradlew test`：运行全部测试。
- `docker compose up -d`：启动 PostgreSQL + Backend + Nginx（先构建 Web 产物）。

## 代码风格与命名规范
- 遵循 Kotlin 官方风格（`kotlin.code.style=official`），统一 4 空格缩进。
- 包名使用小写（如 `com.personalblog...`），类/Composable 用 `PascalCase`，函数与变量用 `camelCase`，常量用 `UPPER_SNAKE_CASE`。
- 依赖版本统一维护在 `gradle/libs.versions.toml`，避免在模块内硬编码版本。

## 测试规范
- 主要测试框架：`kotlin("test")`；后端补充 `ktor-server-test-host`。
- 测试建议放置：`backend/src/test/kotlin`、`frontend/composeApp/src/commonTest/kotlin`、`shared/src/commonTest/kotlin`。
- 测试文件命名使用 `*Test.kt`（示例：`AuthRoutesTest.kt`），优先覆盖接口行为与 DTO 序列化。

## 提交与 Pull Request 规范
当前工作区无法读取 Git 历史，默认采用 Conventional Commits：`feat:`、`fix:`、`chore:`、`docs:`，标题使用简洁祈使句。

PR 至少包含：变更范围、影响模块、已执行测试命令及结果、关联 issue/spec；涉及 UI 的变更请附截图；涉及迁移脚本或 `.env` 变更请单独说明。

## 安全与配置提示
本地开发先复制 `.env.example` 为 `.env`。严禁提交密钥与生产凭据；非本地环境务必使用高强度 `JWT_SECRET`。


## 偏好
1. 数据链路图倾向使用 ASCII 绘制，并且从整体到局部；函数调用图
