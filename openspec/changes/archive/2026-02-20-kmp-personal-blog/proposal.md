## Why

个人博客缺乏一个跨平台统一的解决方案——现有方案要么只支持 Web，要么需要为每个平台单独维护代码。本项目基于 Kotlin Multiplatform（KMP）+ Compose Multiplatform 构建，一套代码同时运行在 Android、iOS、macOS 和 Web 上，配合 Ktor 后端，实现完整的个人博客系统。

## What Changes

- 新建 KMP 多平台前端项目（Android、iOS、macOS、Web）
- 新建 Ktor + Exposed + PostgreSQL 后端服务
- 实现极简风格 UI（参考 AstroPaper 设计稿）
- 支持 Markdown 文章解析与渲染（多端统一）
- 支持日间/夜间/跟随系统三种主题模式
- 实现博客列表、文章阅读、全文搜索核心功能
- 实现 Tags 墙与按标签筛选文章
- 实现 About 页面
- 实现用户管理系统（注册、登录、权限控制）
- 实现后台管理（文章 CRUD、在线编辑、标签管理、用户管理）
- 实现评论系统
- 支持 Docker 一键部署

## Capabilities

### New Capabilities

- `blog-list`: 博客文章列表展示，支持分页、排序
- `blog-reader`: 单篇文章阅读页，Markdown 渲染，支持目录导航
- `blog-search`: 全文搜索，按标题、内容、标签检索文章
- `tags-wall`: Tags 墙展示所有标签，点击标签筛选对应文章列表
- `about-page`: 个人介绍页面，静态内容展示
- `theme-mode`: 日间/夜间/跟随系统主题切换，持久化用户偏好
- `user-auth`: 用户注册、登录、JWT 鉴权、权限角色管理
- `admin-panel`: 后台管理界面，文章 CRUD、在线 Markdown 编辑器、标签管理（最多5个）、用户管理
- `comment-system`: 文章评论的发布、展示、管理（需登录）
- `backend-api`: Ktor REST API 服务，Exposed ORM，PostgreSQL 数据库
- `docker-deploy`: Docker Compose 一键部署配置，包含前端、后端、数据库

### Modified Capabilities

（无已有 spec，全部为新建）

## Impact

- **前端**：新建 KMP 多模块项目，共享业务逻辑层（commonMain），各平台适配层（androidMain、iosMain、desktopMain、wasmJsMain）
- **后端**：新建 Ktor Server 项目，REST API，JWT 认证，Exposed + PostgreSQL
- **依赖**：
  - Compose Multiplatform（UI）
  - Ktor Client（网络请求）
  - Coil / Kamel（KMP 图片加载）
  - Multiplatform Markdown 解析库（如 `multiplatform-markdown-renderer` 或 `Markwon` 替代方案）
  - Ktor Server + Exposed + PostgreSQL Driver
  - Docker + Docker Compose（部署）
- **数据库**：PostgreSQL，需初始化 schema（文章、标签、用户、评论表）
- **部署**：Docker Compose 编排前端静态资源（Web）、后端服务、PostgreSQL
