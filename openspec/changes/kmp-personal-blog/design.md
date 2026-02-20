## Context

本项目是一个全新的个人博客系统，从零开始构建。前端基于 Kotlin Multiplatform + Compose Multiplatform，目标平台为 Android、iOS、macOS、Web（Wasm）。后端基于 Ktor Server + Exposed + PostgreSQL。UI 风格参考 AstroPaper（极简风），支持日间/夜间/跟随系统三种主题。

当前状态：空项目目录，无任何已有代码。

## Goals / Non-Goals

**Goals:**
- 一套 KMP 代码覆盖 Android、iOS、macOS、Web 四端
- 极简 UI，忠实还原 AstroPaper 设计风格
- 完整博客功能：列表、阅读、搜索、Tags、About
- 用户系统 + 后台管理 + 评论系统
- Docker Compose 一键部署

**Non-Goals:**
- 不支持 Windows 桌面端（可后续扩展）
- 不实现 CDN / 对象存储（图片使用 URL 引用）
- 不实现邮件通知系统
- 不实现多语言国际化（i18n）

## Decisions

### 决策 1：项目模块结构

采用 Gradle 多模块结构，分为前端（`frontend/`）和后端（`backend/`）两个独立子项目，共享数据模型通过 `shared/` 模块提供。

```
personal_blog/
├── shared/          # KMP 共享数据模型（DTO、枚举）
├── frontend/        # Compose Multiplatform 前端
│   ├── composeApp/  # 主 KMP 模块
│   └── ...
├── backend/         # Ktor Server 后端
└── docker/          # Docker Compose 配置
```

**备选方案**：单一 Gradle 项目 + 所有模块平铺。
**选择理由**：前后端分离便于独立构建和部署，`shared/` 模块避免 DTO 重复定义。

---

### 决策 2：前端分层架构

前端采用 **MVI（Model-View-Intent）** 架构，分为三层：

- **UI 层**（`composeApp/src/commonMain/ui/`）：Compose 组件，纯展示，无业务逻辑
- **ViewModel 层**（`composeApp/src/commonMain/viewmodel/`）：持有 UI State，处理用户 Intent，调用 Repository
- **Data 层**（`composeApp/src/commonMain/data/`）：Repository + Remote DataSource（Ktor Client）

**备选方案**：MVVM。
**选择理由**：MVI 的单向数据流在 Compose 中天然契合，State 不可变，便于调试和测试。

---

### 决策 3：Markdown 解析库

使用 **`multiplatform-markdown-renderer`**（基于 Compose，支持 commonMain）。

**备选方案**：
- `Markwon`：仅支持 Android，不适用 KMP
- 自研解析器：成本过高

**选择理由**：`multiplatform-markdown-renderer` 直接输出 Compose UI，无需桥接层，支持所有目标平台。

---

### 决策 4：图片加载库

使用 **Coil 3**（支持 KMP，`io.coil-kt.coil3`）。

**备选方案**：Kamel（社区维护，更新频率低）。
**选择理由**：Coil 3 官方支持 KMP，与 Compose Multiplatform 集成成熟，社区活跃。

---

### 决策 5：网络层

前端使用 **Ktor Client**（`io.ktor:ktor-client-core`），各平台引擎：
- Android：`ktor-client-okhttp`
- iOS / macOS：`ktor-client-darwin`
- Web（Wasm）：`ktor-client-js`

序列化使用 `kotlinx.serialization`。

---

### 决策 6：主题系统

主题状态存储在 `commonMain` 的 `ThemeRepository` 中，使用 `multiplatform-settings`（`com.russhwolf:multiplatform-settings`）持久化用户偏好到各平台本地存储。

三种模式：`LIGHT`、`DARK`、`SYSTEM`。`SYSTEM` 模式通过各平台 `expect/actual` 获取系统深色模式状态。

---

### 决策 7：后端 API 设计

采用 **REST API**，版本前缀 `/api/v1/`。认证使用 **JWT**（`io.ktor:ktor-server-auth-jwt`），Token 存储在客户端（HTTP Header `Authorization: Bearer <token>`）。

主要端点：
```
GET    /api/v1/posts          # 文章列表（分页）
GET    /api/v1/posts/{id}     # 文章详情
GET    /api/v1/posts/search   # 全文搜索
GET    /api/v1/tags           # 标签列表
GET    /api/v1/tags/{slug}    # 标签下的文章
POST   /api/v1/auth/login     # 登录
POST   /api/v1/auth/register  # 注册
POST   /api/v1/comments       # 发表评论（需认证）
GET    /api/v1/comments/{postId} # 获取评论列表

# 管理端（需 ADMIN 角色）
POST   /api/v1/admin/posts    # 创建文章
PUT    /api/v1/admin/posts/{id} # 更新文章
DELETE /api/v1/admin/posts/{id} # 删除文章
GET    /api/v1/admin/users    # 用户列表
```

---

### 决策 8：数据库 Schema

核心表设计：

```sql
-- 用户表
users (id, username, email, password_hash, role, created_at)

-- 文章表
posts (id, title, slug, summary, content, cover_image_url, published, created_at, updated_at, author_id)

-- 标签表
tags (id, name, slug)

-- 文章-标签关联（每篇文章最多 5 个标签）
post_tags (post_id, tag_id)

-- 评论表
comments (id, post_id, user_id, content, created_at, is_deleted)
```

全文搜索使用 PostgreSQL 内置的 `tsvector` + `tsquery`，对 `title` 和 `content` 建立 GIN 索引。

---

### 决策 9：导航路由

使用 **Compose Navigation**（`org.jetbrains.androidx.navigation:navigation-compose`，KMP 版本）管理页面路由。

路由定义：
```
/           → 首页（文章列表）
/post/{id}  → 文章阅读
/search     → 搜索
/tags       → Tags 墙
/tags/{slug}→ 标签文章列表
/about      → About 页面
/admin      → 后台管理（需认证）
/login      → 登录页
```

---

### 决策 10：Docker 部署

使用 **Docker Compose** 编排三个服务：
- `backend`：Ktor Server JAR，暴露 8080 端口
- `db`：PostgreSQL 15，持久化 volume
- `nginx`：反向代理，托管 Web 前端静态资源，转发 `/api/` 到 backend

```yaml
# docker-compose.yml 结构示意
services:
  db:      postgres:15
  backend: ktor-app (depends_on: db)
  nginx:   nginx:alpine (depends_on: backend)
```

## Risks / Trade-offs

- **Wasm 平台成熟度** → Compose Multiplatform for Web（Wasm）仍处于 Alpha/Beta 阶段，部分 API 可能不稳定。缓解：锁定 Compose Multiplatform 版本，避免频繁升级。
- **iOS 构建复杂度** → KMP iOS 需要 Xcode 环境，CI 配置较复杂。缓解：优先保证 Android/Web/macOS 可用，iOS 作为后续完善项。
- **全文搜索性能** → PostgreSQL `tsvector` 对中文支持有限（需安装 `zhparser` 或使用 `pg_jieba`）。缓解：初期使用 `ILIKE` 模糊搜索，后续按需引入中文分词扩展。
- **标签数量限制（最多5个）** → 在后端 API 层校验，前端编辑器同步限制，超出时返回 400 错误。
- **JWT 无状态登出** → JWT 本身无法主动失效。缓解：使用短期 Token（1小时）+ Refresh Token，或维护服务端黑名单（Redis，初期可跳过）。

## Migration Plan

1. 初始化 Gradle 多模块项目结构
2. 配置 PostgreSQL 并执行初始化 SQL（`docker/init.sql`）
3. 启动后端服务，验证 API 端点
4. 构建前端各平台产物（Android APK、iOS IPA、macOS App、Web 静态资源）
5. 执行 `docker compose up -d` 完成部署
6. 回滚策略：`docker compose down`，恢复上一版本镜像

## Open Questions

- Compose Multiplatform for Web（Wasm）的 SEO 支持方案？（当前 Wasm 不支持 SSR，可能影响搜索引擎收录）
- 评论系统是否需要审核机制（先审后发 vs 先发后审）？
- 后台管理是否需要独立的 Web 应用，还是集成在同一 KMP 应用中？
