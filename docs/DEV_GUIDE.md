# Personal Blog - 开发环境快速指南

## 🚀 快速开始

### 启动开发环境
```bash
./start-dev.sh
```

### 查看系统状态
```bash
./status.sh
```

### 停止开发环境
```bash
./stop-dev.sh
```

## 📱 Android 开发

### 安装应用到模拟器
```bash
./gradlew :composeApp:installDebug
```

### 构建 Release 版本
```bash
./gradlew :composeApp:assembleRelease
```

### 运行测试
```bash
./gradlew :composeApp:test
```

## 🔧 后端开发

### 手动启动后端
```bash
./gradlew :backend:run
```

### 查看后端日志
```bash
tail -f /tmp/backend.log
```

### 运行后端测试
```bash
./gradlew :backend:test
```

## 🗄️ 数据库管理

### 连接数据库
```bash
/opt/homebrew/opt/postgresql@15/bin/psql personalblog
```

### 常用 SQL 命令
```sql
-- 查看所有文章
SELECT id, title, published FROM posts;

-- 查看所有用户
SELECT id, username, email, role FROM users;

-- 清空文章数据
TRUNCATE posts, post_tags CASCADE;

-- 重置数据库
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

### 数据库备份
```bash
/opt/homebrew/opt/postgresql@15/bin/pg_dump personalblog > backup.sql
```

### 数据库恢复
```bash
/opt/homebrew/opt/postgresql@15/bin/psql personalblog < backup.sql
```

## 🌐 API 测试

### 获取文章列表
```bash
curl http://localhost:8080/api/v1/posts
```

### 登录获取 Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@blog.com","password":"admin123"}'
```

### 创建文章（需要 Token）
```bash
TOKEN="your-token-here"
curl -X POST http://localhost:8080/api/v1/admin/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "测试文章",
    "content": "文章内容",
    "published": true
  }'
```

## 🔐 测试账号

- **邮箱**: admin@blog.com
- **密码**: admin123
- **角色**: ADMIN

## 📝 开发注意事项

### API 地址配置

**Android 模拟器**: `http://10.0.2.2:8080`
- 修改位置: `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/data/remote/PlatformConfig.kt`

**生产环境**: 修改为实际域名
```kotlin
var apiBaseUrl: String = "https://your-domain.com"
```

### 网络安全配置

开发环境允许明文 HTTP（仅用于本地测试）：
- 配置文件: `frontend/composeApp/src/androidMain/res/xml/network_security_config.xml`
- 生产环境请移除明文 HTTP 配置，只允许 HTTPS

### 常见问题

**Q: 后端启动失败，提示数据库连接错误？**
A: 确保 PostgreSQL 服务已启动：`brew services start postgresql@15`

**Q: Android 应用无法连接后端？**
A: 检查以下几点：
1. 后端服务是否运行在 8080 端口
2. API 地址是否配置为 `http://10.0.2.2:8080`
3. 网络权限和安全配置是否正确

**Q: 登录失败，提示 401 Unauthorized？**
A: 确认密码是否正确，或重新注册账号后将角色改为 ADMIN：
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@blog.com';
```

## 🏗️ 项目结构

```
personal_blog/
├── backend/              # Ktor 后端服务
│   ├── src/main/kotlin/
│   │   ├── Application.kt
│   │   ├── routes/       # API 路由
│   │   ├── repository/   # 数据访问层
│   │   └── db/           # 数据库配置
│   └── build.gradle.kts
├── frontend/composeApp/  # Compose Multiplatform 前端
│   ├── src/
│   │   ├── commonMain/   # 跨平台共享代码
│   │   ├── androidMain/  # Android 特定代码
│   │   ├── iosMain/      # iOS 特定代码
│   │   └── wasmJsMain/   # Web 特定代码
│   └── build.gradle.kts
├── shared/               # 前后端共享 DTO
│   └── src/commonMain/kotlin/
├── docker/               # Docker 配置
├── start-dev.sh          # 启动脚本
├── stop-dev.sh           # 停止脚本
└── status.sh             # 状态检查脚本
```

## 🚢 部署

### Docker 部署（推荐）

1. 构建前端：
```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

2. 启动所有服务：
```bash
docker compose up -d
```

3. 查看日志：
```bash
docker compose logs -f
```

### 手动部署

1. 构建后端 JAR：
```bash
./gradlew :backend:build
```

2. 运行后端：
```bash
java -jar backend/build/libs/backend-all.jar
```

3. 配置 Nginx 托管前端静态文件

## 📚 更多资源

- [Kotlin Multiplatform 文档](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform 文档](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor 文档](https://ktor.io/docs/)
- [Exposed ORM 文档](https://github.com/JetBrains/Exposed)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License
