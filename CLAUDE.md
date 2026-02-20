# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个使用 Kotlin Multiplatform (KMP) + Compose Multiplatform 构建的跨平台个人博客系统。

技术栈：
- 后端：Kotlin + Ktor Server + PostgreSQL + Exposed ORM + JWT 认证
- 前端：Compose Multiplatform (支持 Android, iOS, macOS, Web/WASM)
- 共享：Kotlin Multiplatform 共享数据模型
- 构建工具：Gradle 8.10.2 + AGP 8.5.0

## 重要配置说明

### 依赖仓库
项目使用阿里云镜像加速依赖下载（在 `settings.gradle.kts` 中配置），适合国内网络环境：
- Google Maven: https://maven.aliyun.com/repository/google
- Maven Central: https://maven.aliyun.com/repository/public
- Gradle Plugin: https://maven.aliyun.com/repository/gradle-plugin

如果网络环境良好可以访问 Google，可以改回官方仓库：
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

### 性能优化
`gradle.properties` 已配置编译优化：
- 并行构建、增量编译、构建缓存已启用
- JVM 堆内存设置为 4GB
- Kotlin 增量编译和缓存已启用
- Android 构建特性优化

### 已知问题和解决方案

#### 时间戳字段
数据库表使用 `timestamp` 而非 `timestampWithTimeZone`，在代码中使用 `Clock.System.now()` 赋值。

#### macOS/iOS 依赖问题
如果遇到 macOS/iOS 平台依赖下载失败，可以：
1. 只构建后端和 Web：`./gradlew :backend:build :composeApp:wasmJsBrowserDistribution`
2. 或者临时移除 macOS/iOS target（在 `frontend/composeApp/build.gradle.kts` 中注释掉相关配置）

#### Yarn Lock 更新
如果遇到 "Lock file was changed" 错误，运行：
```bash
./gradlew kotlinUpgradeYarnLock
```

#### 前端编译注意事项
- `inline` 函数不能访问 `private` 或 `internal` 属性，需要使用 `public`
- `expect/actual` 函数的注解必须完全匹配（包括 `@Composable`）
- PagedResponse 使用 `page` 字段而不是 `currentPage`
- Tag 对象需要使用 `tag.name` 而不是直接使用 `tag`

## 项目结构

```
personal_blog/
├── backend/              # Ktor 后端服务 (JVM)
│   ├── src/main/kotlin/com/personalblog/backend/
│   │   ├── Application.kt           # 应用入口
│   │   ├── plugins/Plugins.kt       # Ktor 插件配置 (CORS, Auth, JSON)
│   │   ├── auth/JwtConfig.kt        # JWT 配置
│   │   ├── db/                      # 数据库层
│   │   │   ├── DatabaseFactory.kt   # 数据库连接
│   │   │   └── Tables.kt            # Exposed 表定义
│   │   ├── repository/              # 数据访问层
│   │   └── routes/                  # API 路由
│   └── src/main/resources/
│       ├── application.conf         # Ktor 配置
│       └── db/migration/            # Flyway 数据库迁移
├── frontend/composeApp/  # Compose Multiplatform 前端
│   └── src/
│       ├── commonMain/kotlin/       # 跨平台共享代码
│       │   └── com/personalblog/app/
│       │       ├── ui/
│       │       │   ├── screen/      # 页面
│       │       │   ├── viewmodel/   # ViewModel
│       │       │   ├── components/  # 可复用组件
│       │       │   ├── navigation/  # 导航配置
│       │       │   └── theme/       # 主题配置
│       ├── androidMain/             # Android 特定代码
│       ├── iosMain/                 # iOS 特定代码
│       └── wasmJsMain/              # Web 特定代码
└── shared/               # 前后端共享数据模型 (DTO)
    └── src/commonMain/kotlin/com/personalblog/shared/dto/
```

## 构建和运行命令

### 后端开发
```bash
# 本地运行后端 (需要 PostgreSQL)
./gradlew :backend:run

# 构建后端 JAR
./gradlew :backend:build
```

### 前端开发
```bash
# Web 开发模式 (WASM) - 注意：项目名是 :composeApp 不是 :frontend:composeApp
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
# 访问 http://localhost:8080/

# Web 生产构建
./gradlew :composeApp:wasmJsBrowserDistribution

# Android
./gradlew :composeApp:installDebug

# Desktop (macOS/Linux/Windows)
./gradlew :composeApp:run
```

**重要提示**：虽然前端代码在 `frontend/composeApp/` 目录下，但 Gradle 项目名称是 `:composeApp`（在 `settings.gradle.kts` 中配置）。所有 Gradle 命令都应使用 `:composeApp` 而不是 `:frontend:composeApp`。

### Docker 部署
```bash
# 首次部署前需要构建前端
./gradlew :composeApp:wasmJsBrowserDistribution

# 启动所有服务 (PostgreSQL + Backend + Nginx)
docker compose up -d

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

访问地址：
- 前端: http://localhost
- 后端 API: http://localhost/api/v1 (通过 Nginx 反向代理)
- 直接访问后端: http://localhost:8080

### 测试
```bash
# 运行所有测试
./gradlew test

# 运行后端测试
./gradlew :backend:test

# 运行前端测试
./gradlew :composeApp:test
```

## Gradle 项目结构说明

项目包含三个 Gradle 子项目：
- `:backend` - 后端服务（物理路径：`backend/`）
- `:shared` - 共享模块（物理路径：`shared/`）
- `:composeApp` - 前端应用（物理路径：`frontend/composeApp/`）

查看所有项目：`./gradlew projects`

## 架构要点

### 三层模块架构
1. **shared 模块**：定义前后端共享的 DTO (Data Transfer Objects)，使用 Kotlin Multiplatform 编译到所有目标平台
2. **backend 模块**：纯 JVM 模块，依赖 shared，使用 Exposed ORM 与数据库交互
3. **frontend/composeApp 模块**：Multiplatform 模块，依赖 shared，使用 Compose Multiplatform 构建 UI

### 后端架构
- **Application.kt**：应用入口，初始化数据库、配置插件、注册路由
- **Repository 层**：封装数据库操作，使用 Exposed DSL
- **Routes 层**：定义 RESTful API 端点，分为公开接口、认证接口、管理员接口
- **JWT 认证**：使用 `jwt-auth` 配置，从 token 中提取 userId 和 role
- **数据库迁移**：使用 Flyway，迁移文件位于 `backend/src/main/resources/db/migration/`

### 前端架构
- **MVVM 模式**：Screen (View) + ViewModel + Repository
- **导航**：使用 Jetpack Navigation Compose，路由定义在 `Screen.kt`
- **平台特定代码**：通过 `expect/actual` 机制实现，例如主题适配
- **网络请求**：使用 Ktor Client，跨平台支持 (OkHttp/Darwin/JS)

### 环境变量配置
后端通过环境变量配置：
- `DB_URL` / `DB_HOST` + `DB_PORT` + `DB_NAME`：数据库连接
- `DB_USER` / `DB_PASSWORD`：数据库凭证
- `JWT_SECRET` / `JWT_ISSUER` / `JWT_AUDIENCE`：JWT 配置

参考 `.env.example` 和 `docker-compose.yml`

## 开发注意事项

### 修改共享数据模型
1. 在 `shared/src/commonMain/kotlin/` 中修改 DTO
2. 重新构建 shared 模块：`./gradlew :shared:build`
3. 前后端会自动使用更新后的模型

### 数据库迁移
1. 在 `backend/src/main/resources/db/migration/` 创建新的 SQL 文件
2. 命名格式：`V{version}__{description}.sql` (例如 `V2__add_user_avatar.sql`)
3. 重启后端，Flyway 会自动执行迁移

### 添加新的 API 端点
1. 在 `backend/src/main/kotlin/com/personalblog/backend/routes/` 创建或修改路由文件
2. 在 `Application.kt` 的 `routing {}` 块中注册路由
3. 如需认证，使用 `authenticate("jwt-auth") {}` 包裹路由

### 前端跨平台开发
- 共享代码放在 `commonMain`
- 平台特定代码使用 `expect/actual`：
  - `commonMain` 中声明 `expect` 函数/类
  - `androidMain`/`iosMain`/`wasmJsMain` 中实现 `actual` 函数/类

### Gradle 版本目录
依赖版本统一在 `gradle/libs.versions.toml` 中管理，使用 `libs.` 前缀引用

## 默认管理员账号
- Email: admin@example.com
- Password: admin123

(由 `docker/init.sql` 初始化)
