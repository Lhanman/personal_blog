# KMP Personal Blog

一个使用 Kotlin Multiplatform (KMP) + Compose Multiplatform 构建的跨平台个人博客系统。

## 技术栈

### 后端
- Kotlin + Ktor Server
- PostgreSQL + Exposed ORM
- JWT 认证
- Flyway 数据库迁移

### 前端
- Kotlin Multiplatform
- Compose Multiplatform (Android, iOS, macOS, Web)
- Ktor Client
- Material 3 Design

## 快速开始

### 前置要求
- Docker & Docker Compose
- JDK 17+
- Gradle 8.5+

### 一键部署

1. 克隆仓库
```bash
git clone <repository-url>
cd personal_blog
```

2. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，修改数据库密码和 JWT Secret
```

3. 构建前端
```bash
./gradlew :frontend:composeApp:wasmJsBrowserDistribution
```

4. 启动所有服务
```bash
docker compose up -d
```

5. 访问应用
- 前端: http://localhost
- 后端 API: http://localhost/api/v1

### 默认管理员账号
- Email: admin@example.com
- Password: admin123

## 开发

### 后端开发
```bash
./gradlew :backend:run
```

### 前端开发
```bash
# Android
./gradlew :frontend:composeApp:installDebug

# Desktop
./gradlew :frontend:composeApp:run

# Web
./gradlew :frontend:composeApp:wasmJsBrowserDevelopmentRun
```

## 项目结构

```
personal_blog/
├── backend/              # Ktor 后端
│   ├── src/
│   └── Dockerfile
├── frontend/
│   └── composeApp/      # Compose Multiplatform 前端
├── shared/              # 共享数据模型
├── docker/              # Docker 配置
│   ├── init.sql
│   └── nginx.conf
└── docker-compose.yml
```

## API 文档

### 公开接口
- `GET /api/v1/posts` - 获取文章列表（分页）
- `GET /api/v1/posts/{id}` - 获取文章详情
- `GET /api/v1/posts/search?q=keyword` - 搜索文章
- `GET /api/v1/tags` - 获取所有标签
- `GET /api/v1/tags/{slug}` - 获取标签下的文章
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录

### 认证接口
- `GET /api/v1/comments/{postId}` - 获取评论
- `POST /api/v1/comments` - 发表评论（需登录）

### 管理员接口
- `POST /api/v1/admin/posts` - 创建文章
- `PUT /api/v1/admin/posts/{id}` - 更新文章
- `DELETE /api/v1/admin/posts/{id}` - 删除文章
- `GET /api/v1/admin/users` - 获取用户列表
- `PUT /api/v1/admin/users/{id}/role` - 更新用户角色

## 许可证

MIT License
