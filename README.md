# Personal Blog

一个使用 Kotlin Multiplatform + Compose Multiplatform 构建的跨平台个人博客系统。

## ✨ 特性

- 🚀 **跨平台支持**: Android、iOS、macOS、Windows、Web (WASM)
- 🎨 **现代 UI**: 使用 Compose Multiplatform 和 Material Design 3
- 🔐 **用户认证**: JWT 认证，支持管理员和普通用户角色
- 📝 **Markdown 支持**: 文章内容使用 Markdown 编写和渲染
- 🔍 **全文搜索**: 基于 PostgreSQL 的全文搜索功能
- 🏷️ **标签系统**: 文章标签分类和筛选
- 💬 **评论系统**: 用户可以对文章发表评论
- 🌓 **主题切换**: 支持浅色/深色/跟随系统主题
- 📱 **响应式设计**: 适配不同屏幕尺寸

## 🛠️ 技术栈

### 后端
- **Kotlin** + **Ktor Server**: RESTful API
- **PostgreSQL**: 数据库
- **Exposed ORM**: 数据库访问
- **JWT**: 身份认证
- **BCrypt**: 密码加密
- **Flyway**: 数据库迁移

### 前端
- **Kotlin Multiplatform**: 跨平台代码共享
- **Compose Multiplatform**: 声明式 UI
- **Ktor Client**: 网络请求
- **Coil 3**: 图片加载
- **Navigation Compose**: 路由导航
- **Multiplatform Settings**: 本地存储
- **Markdown Renderer**: Markdown 渲染

### 部署
- **Docker**: 容器化部署
- **Nginx**: 反向代理和静态文件托管
- **Gradle**: 构建工具

## 📦 快速开始

### 前置要求

- JDK 17+
- Gradle 8.10+
- PostgreSQL 15+ (或使用 Docker)
- Android Studio (用于 Android 开发)
- Xcode (用于 iOS/macOS 开发，仅 macOS)

### 本地开发环境

1. **克隆项目**
```bash
git clone <repository-url>
cd personal_blog
```

2. **启动开发环境**
```bash
./start-dev.sh
```

3. **安装 Android 应用**
```bash
./gradlew :composeApp:installDebug
```

4. **访问应用**
- Android: 在模拟器或真机上打开应用
- Web: http://localhost:8080 (开发模式)
- 后端 API: http://localhost:8080/api/v1

### Docker 部署

1. **配置环境变量**
```bash
cp .env.example .env
# 编辑 .env 文件，修改数据库密码和 JWT Secret
```

2. **构建前端**
```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

3. **启动所有服务**
```bash
docker compose up -d
```

4. **访问应用**
- 前端: http://localhost
- 后端 API: http://localhost/api/v1

### 默认管理员账号

- **邮箱**: admin@blog.com
- **密码**: admin123
- **角色**: ADMIN

## 🔧 开发

### 快速命令

```bash
# 查看系统状态
./status.sh

# 启动开发环境
./start-dev.sh

# 停止开发环境
./stop-dev.sh
```

### 后端开发

```bash
# 运行后端
./gradlew :backend:run

# 运行测试
./gradlew :backend:test

# 构建 JAR
./gradlew :backend:build
```

### 前端开发

```bash
# Android
./gradlew :composeApp:installDebug

# Web (开发模式)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (生产构建)
./gradlew :composeApp:wasmJsBrowserDistribution

# Desktop (macOS / Windows / Linux)
./gradlew :composeApp:run

# 为当前操作系统打包桌面安装产物
./gradlew :composeApp:packageDistributionForCurrentOS
```

### 前端日志系统

- 前端已接入统一日志系统，公共代码位于 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/logging/`
- Android 日志默认写入应用私有目录 `filesDir/logs/`
- Desktop 日志默认写入系统应用数据目录下的 `logs/`
- iOS / macOS Native 日志写入应用沙盒目录下的 `logs/`
- Web 日志默认使用 `IndexedDB` 持久化，并在不可用时降级为 `console`

当前 Web 日志治理支持以下配置项：

- `minPersistLevel`：最小持久化级别
- `maxTotalBytes`：日志总容量上限
- `maxRecordCount`：最大日志条数
- `maxPerEntryBytes`：单条日志最大大小
- `retentionDays`：日志保留天数
- `softLimitBytes` / `hardLimitBytes`：软/硬清理阈值
- `cleanupOnStartup`：启动时清理
- `cleanupOnWriteThreshold`：累计写入阈值触发清理
- `alwaysPersistLevels`：优先保留的级别
- `redactKeys`：敏感字段脱敏列表

### 数据库管理

```bash
# 连接数据库
/opt/homebrew/opt/postgresql@15/bin/psql personalblog

# 查看状态
./status.sh
```

详细开发指南请查看 [DEV_GUIDE.md](docs/DEV_GUIDE.md)

## 🏗️ 项目结构

```
personal_blog/
├── backend/                    # 后端服务 (Ktor)
│   ├── src/main/kotlin/
│   │   ├── Application.kt      # 应用入口
│   │   ├── auth/               # JWT 认证
│   │   ├── db/                 # 数据库配置
│   │   ├── repository/         # 数据访问层
│   │   ├── routes/             # API 路由
│   │   └── plugins/            # Ktor 插件
│   └── src/main/resources/
│       └── db/migration/       # Flyway 迁移脚本
├── frontend/composeApp/        # 前端应用
│   └── src/
│       ├── commonMain/         # 跨平台共享代码
│       │   ├── ui/
│       │   │   ├── screen/     # 页面
│       │   │   ├── viewmodel/  # ViewModel
│       │   │   ├── components/ # 可复用组件
│       │   │   ├── navigation/ # 导航
│       │   │   └── theme/      # 主题
│       │   └── data/           # 数据层
│       ├── androidMain/        # Android 特定
│       ├── iosMain/            # iOS 特定
│       └── wasmJsMain/         # Web 特定
├── shared/                     # 共享数据模型
│   └── src/commonMain/kotlin/
│       └── dto/                # DTO 定义
├── docker/                     # Docker 配置
│   ├── Dockerfile              # 后端镜像
│   ├── nginx.conf              # Nginx 配置
│   └── init.sql                # 数据库初始化
├── start-dev.sh                # 启动脚本
├── stop-dev.sh                 # 停止脚本
├── status.sh                   # 状态检查
├── docker-compose.yml          # Docker Compose
└── DEV_GUIDE.md                # 开发指南
```

## 📚 API 文档

### 公开接口

- `GET /api/v1/posts` - 获取文章列表（分页）
- `GET /api/v1/posts/{id}` - 获取文章详情
- `GET /api/v1/posts/search?q={query}` - 搜索文章
- `GET /api/v1/tags` - 获取所有标签
- `GET /api/v1/tags/{slug}` - 获取标签下的文章
- `GET /api/v1/comments/{postId}` - 获取文章评论

### 认证接口

- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录

### 需要认证的接口

- `POST /api/v1/comments` - 发表评论

### 管理员接口

- `POST /api/v1/admin/posts` - 创建文章
- `PUT /api/v1/admin/posts/{id}` - 更新文章
- `DELETE /api/v1/admin/posts/{id}` - 删除文章
- `GET /api/v1/admin/users` - 获取用户列表
- `PUT /api/v1/admin/users/{id}/role` - 更新用户角色

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行后端测试
./gradlew :backend:test

# 运行前端测试
./gradlew :composeApp:test
```

## 🚢 部署

### 环境变量

在 `.env` 文件中配置（参考 `.env.example`）：

```env
# 数据库配置
DB_HOST=db
DB_PORT=5432
DB_NAME=personalblog
DB_USER=postgres
DB_PASSWORD=your-secure-password

# JWT 配置
JWT_SECRET=your-jwt-secret
JWT_ISSUER=personal-blog
JWT_AUDIENCE=personal-blog-users
```

### Docker 部署步骤

1. 构建前端静态文件
2. 配置环境变量
3. 启动 Docker Compose
4. 访问应用

详细步骤请参考上面的"快速开始"部分。

## 📱 平台支持

| 平台 | 状态 | 说明 |
|------|------|------|
| Android | ✅ 完全支持 | API 21+ |
| iOS | ✅ 完全支持 | iOS 14+ |
| macOS | ✅ 完全支持 | 原生 iOS/macOS target + Desktop JVM |
| Windows | ✅ 完全支持 | Desktop JVM |
| Web (WASM) | ✅ 完全支持 | 现代浏览器 |
| Desktop (JVM) | ✅ 完全支持 | Windows/Linux/macOS |

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 待办事项

- [ ] 添加文章草稿功能
- [ ] 实现文章分类功能
- [ ] 添加图片上传功能
- [ ] 实现评论回复功能
- [ ] 添加文章点赞功能
- [ ] 实现 RSS 订阅
- [ ] 添加站点统计功能
- [ ] 优化 SEO
- [ ] 添加 PWA 支持
- [ ] 实现多语言支持

## 🙏 致谢

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [Exposed](https://github.com/JetBrains/Exposed)
- [Coil](https://coil-kt.github.io/coil/)

## 📄 许可证

MIT License

---

⭐ 如果这个项目对你有帮助，请给个 Star！
