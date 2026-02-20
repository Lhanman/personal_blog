## 1. 项目初始化与基础配置

- [x] 1.1 初始化 Gradle 多模块项目结构（`shared/`、`frontend/`、`backend/`、`docker/`）
- [x] 1.2 配置根 `settings.gradle.kts`，注册所有子模块
- [x] 1.3 配置 `shared/` 模块：KMP 库模块，添加 `kotlinx.serialization` 依赖
- [x] 1.4 配置 `frontend/composeApp/` 模块：目标平台 Android、iOS、macOS、wasmJs
- [x] 1.5 配置 `backend/` 模块：JVM 应用，添加 Ktor Server 依赖
- [x] 1.6 配置版本目录（`gradle/libs.versions.toml`），统一管理所有依赖版本

## 2. 共享数据模型（shared 模块）

- [x] 2.1 定义 `PostDto`（id、title、slug、summary、coverImageUrl、publishedAt、tags、readingTimeMinutes）
- [x] 2.2 定义 `TagDto`（id、name、slug、postCount）
- [x] 2.3 定义 `CommentDto`（id、postId、authorName、content、createdAt）
- [x] 2.4 定义 `UserDto`（id、username、email、role）
- [x] 2.5 定义 `AuthRequest` / `AuthResponse`（含 JWT token）
- [x] 2.6 定义分页包装类 `PagedResponse<T>`

## 3. 后端：数据库与基础设施

- [x] 3.1 编写 `docker/init.sql`：创建 users、posts、tags、post_tags、comments 表
- [x] 3.2 在 `backend/` 中配置 Exposed 连接池（HikariCP + PostgreSQL Driver）
- [x] 3.3 用 Exposed DSL 定义所有表对象（`UsersTable`、`PostsTable` 等）
- [x] 3.4 实现数据库迁移脚本（Flyway 或手动 SQL 版本管理）
- [x] 3.5 为 `posts.title` 和 `posts.content` 添加 PostgreSQL GIN 全文索引（`tsvector`）

## 4. 后端：Repository 层

- [x] 4.1 实现 `PostRepository`：分页查询、按 ID 查询、按 slug 查询、全文搜索
- [x] 4.2 实现 `TagRepository`：查询所有标签（含文章计数）、按 slug 查询标签下的文章
- [x] 4.3 实现 `UserRepository`：注册、按 email 查询、按 ID 查询
- [x] 4.4 实现 `CommentRepository`：按 postId 查询评论列表、新增评论、软删除评论

## 5. 后端：API 路由

- [x] 5.1 配置 Ktor Server 基础插件（ContentNegotiation、CORS、StatusPages、CallLogging）
- [x] 5.2 配置 JWT 认证插件（`ktor-server-auth-jwt`），定义 ADMIN / USER 角色
- [x] 5.3 实现 `GET /api/v1/posts` 路由（分页，支持 `page`、`size` 查询参数）
- [x] 5.4 实现 `GET /api/v1/posts/{id}` 路由
- [x] 5.5 实现 `GET /api/v1/posts/search` 路由（`q` 查询参数，全文搜索）
- [x] 5.6 实现 `GET /api/v1/tags` 路由
- [x] 5.7 实现 `GET /api/v1/tags/{slug}` 路由（返回该标签下的文章列表）
- [x] 5.8 实现 `POST /api/v1/auth/register` 和 `POST /api/v1/auth/login` 路由
- [x] 5.9 实现 `GET /api/v1/comments/{postId}` 和 `POST /api/v1/comments` 路由（POST 需认证）
- [x] 5.10 实现管理端路由（需 ADMIN 角色）：文章 CRUD（`POST/PUT/DELETE /api/v1/admin/posts`）
- [x] 5.11 实现管理端路由：用户列表 `GET /api/v1/admin/users`、更新用户角色

## 6. 前端：网络层与 Repository

- [x] 6.1 配置 Ktor Client（各平台引擎 + `kotlinx.serialization` 插件）
- [x] 6.2 实现 `ApiClient`：封装 base URL、JWT Token 注入、统一错误处理
- [x] 6.3 实现 `PostRemoteDataSource`：调用文章相关 API
- [x] 6.4 实现 `TagRemoteDataSource`：调用标签相关 API
- [x] 6.5 实现 `AuthRemoteDataSource`：调用登录/注册 API
- [x] 6.6 实现 `CommentRemoteDataSource`：调用评论相关 API
- [x] 6.7 实现 `TokenRepository`：使用 `multiplatform-settings` 持久化 JWT Token

## 7. 前端：主题系统

- [x] 7.1 定义 `ThemeMode` 枚举（LIGHT、DARK、SYSTEM）
- [x] 7.2 实现 `ThemeRepository`：使用 `multiplatform-settings` 持久化主题偏好
- [x] 7.3 实现各平台 `expect/actual` 获取系统深色模式状态
- [x] 7.4 定义 `AppColorScheme`：极简风格的 Light / Dark 颜色方案（参考 AstroPaper 配色）
- [ ] 7.5 在 `App()` 根组件中接入 `ThemeViewModel`，动态切换 `MaterialTheme`

## 8. 前端：导航与路由

- [ ] 8.1 添加 `navigation-compose`（KMP 版本）依赖
- [x] 8.2 定义所有路由常量（`Screen` sealed class 或 object）
- [x] 8.3 实现 `AppNavHost`：注册所有页面路由（首页、文章、搜索、Tags、About、登录、后台）
- [ ] 8.4 实现底部导航栏 / 侧边导航（移动端 vs 桌面端自适应布局）

## 9. 前端：博客列表页

- [x] 9.1 实现 `BlogListViewModel`（加载文章列表、分页状态管理）
- [x] 9.2 实现 `PostCard` 组件（封面图、标题、摘要、日期、标签、阅读时长）
- [x] 9.3 实现 `BlogListScreen`（列表 + 加载更多 + 空状态）
- [ ] 9.4 使用 Coil 3 加载封面图，实现加载占位符

## 10. 前端：文章阅读页

- [ ] 10.1 添加 `multiplatform-markdown-renderer` 依赖
- [x] 10.2 实现 `BlogReaderViewModel`（加载文章详情）
- [ ] 10.3 实现 `MarkdownContent` 组件（渲染 Markdown，代码块语法高亮）
- [ ] 10.4 实现文章目录（TOC）侧边栏组件，支持点击平滑滚动
- [x] 10.5 实现 `BlogReaderScreen`（元信息 + Markdown 内容 + TOC + 评论区）

## 11. 前端：搜索页

- [x] 11.1 实现 `SearchViewModel`（搜索状态、防抖 300ms、建议列表）
- [x] 11.2 实现搜索输入框组件（带清除按钮）
- [ ] 11.3 实现搜索建议下拉列表（最多 5 条）
- [x] 11.4 实现 `SearchScreen`（搜索结果列表 + 关键词高亮 + 空状态）

## 12. 前端：Tags 页

- [x] 12.1 实现 `TagsViewModel`（加载所有标签）
- [x] 12.2 实现 `TagChip` 组件（标签名 + 文章计数）
- [x] 12.3 实现 `TagsWallScreen`（标签云布局）
- [ ] 12.4 实现 `TagPostsScreen`（某标签下的文章列表，复用 `PostCard`）

## 13. 前端：About 页与主题切换

- [x] 13.1 实现 `AboutScreen`（静态个人介绍内容，支持 Markdown 渲染）
- [ ] 13.2 实现主题切换按钮组件（三态：日间/夜间/跟随系统）
- [ ] 13.3 将主题切换按钮集成到顶部导航栏

## 14. 前端：用户认证

- [x] 14.1 实现 `AuthViewModel`（登录、注册、登出状态管理）
- [x] 14.2 实现 `LoginScreen`（邮箱 + 密码表单，错误提示）
- [x] 14.3 登录成功后持久化 JWT Token，更新全局认证状态
- [ ] 14.4 实现路由守卫：未登录访问需认证页面时重定向至登录页

## 15. 前端：评论系统

- [x] 15.1 实现 `CommentViewModel`（加载评论列表、发表评论）
- [x] 15.2 实现 `CommentItem` 组件（头像占位、用户名、内容、时间）
- [x] 15.3 实现评论输入框组件（需登录才可见，未登录显示"登录后评论"提示）
- [x] 15.4 将评论区集成到 `BlogReaderScreen` 底部

## 16. 前端：后台管理

- [ ] 16.1 实现 `AdminViewModel`（文章列表管理、CRUD 操作）
- [ ] 16.2 实现 `AdminPostListScreen`（文章管理列表，含编辑/删除操作）
- [ ] 16.3 实现在线 Markdown 编辑器组件（左侧编辑、右侧预览双栏布局）
- [ ] 16.4 实现标签选择器组件（最多选 5 个，超出时禁用添加）
- [ ] 16.5 实现 `AdminPostEditScreen`（标题、内容编辑器、标签选择、发布/草稿切换）
- [ ] 16.6 实现 `AdminUserListScreen`（用户列表，支持修改角色）
- [ ] 16.7 后台管理路由添加 ADMIN 角色守卫

## 17. Docker 部署配置

- [x] 17.1 编写 `backend/Dockerfile`（多阶段构建，生成 fat JAR）
- [x] 17.2 编写 `docker/nginx.conf`（托管 Web 静态资源，反向代理 `/api/` 到 backend）
- [x] 17.3 编写 `docker-compose.yml`（db、backend、nginx 三服务编排）
- [x] 17.4 编写 `docker/init.sql`（PostgreSQL 初始化 schema + 默认管理员账号）
- [x] 17.5 编写 `.env.example`（数据库密码、JWT Secret 等环境变量模板）
- [x] 17.6 编写 `README.md`：一键部署步骤说明（`docker compose up -d`）

## 18. 测试与验收

- [ ] 18.1 后端：为 `PostRepository` 编写单元测试（使用 H2 内存数据库）
- [ ] 18.2 后端：为主要 API 路由编写集成测试（`ktor-server-test-host`）
- [ ] 18.3 前端：为 `BlogListViewModel` 编写单元测试（mock Repository）
- [ ] 18.4 前端：为 `SearchViewModel` 编写单元测试（验证防抖逻辑）
- [ ] 18.5 端到端验收：本地启动 Docker Compose，验证所有核心功能可用
