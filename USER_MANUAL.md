# Personal Blog - 用户使用手册

## 📖 目录

1. [快速开始](#快速开始)
2. [系统管理](#系统管理)
3. [内容管理](#内容管理)
4. [用户管理](#用户管理)
5. [常见问题](#常见问题)
6. [故障排除](#故障排除)

---

## 🚀 快速开始

### 首次使用

#### 1. 启动系统

```bash
# 方式一：使用启动脚本（推荐）
./start-dev.sh

# 方式二：手动启动
# 启动 PostgreSQL
brew services start postgresql@15

# 启动后端服务
./gradlew :backend:run &

# 安装 Android 应用
./gradlew :composeApp:installDebug
```

#### 2. 访问系统

- **Android 应用**: 在模拟器或真机上打开应用
- **Web 应用**: http://localhost:8080 (开发模式)
- **后端 API**: http://localhost:8080/api/v1

#### 3. 登录系统

使用默认管理员账号登录：

```
邮箱: admin@blog.com
密码: admin123
```

**重要提示**: 首次登录后请立即修改密码！

---

## 🛠️ 系统管理

### 查看系统状态

```bash
./status.sh
```

显示内容：
- PostgreSQL 运行状态
- 后端服务状态
- API 响应状态
- 数据库内容统计
- Android 应用状态

### 启动和停止服务

```bash
# 启动开发环境
./start-dev.sh

# 停止开发环境
./stop-dev.sh

# 查看后端日志
tail -f /tmp/backend.log
```

### 数据库管理

```bash
# 启动数据库管理工具
./db-manager.sh
```

功能菜单：
1. 查看数据库状态
2. 查看所有文章
3. 查看所有用户
4. 查看所有标签
5. 创建测试数据
6. 清空所有数据
7. 备份数据库
8. 恢复数据库
9. 连接数据库 (psql)

### API 测试

```bash
# 运行完整的 API 测试套件
./test-api.sh
```

测试内容：
- API 连接测试
- 用户登录测试
- 文章列表获取
- 标签列表获取
- 文章创建测试
- 搜索功能测试
- 用户注册测试
- 性能测试（100 次请求）

---

## 📝 内容管理

### 创建文章

#### 通过 Web 界面

1. 登录系统
2. 进入"后台管理"
3. 点击"新建文章"
4. 填写文章信息：
   - 标题（必填）
   - URL 别名（可选，自动生成）
   - 摘要（可选）
   - 内容（必填，支持 Markdown）
   - 封面图片 URL（可选）
   - 标签（最多 5 个）
5. 选择"发布"或"保存草稿"
6. 点击"提交"

#### 通过 API

```bash
# 1. 登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@blog.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. 创建文章
curl -X POST http://localhost:8080/api/v1/admin/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "我的第一篇文章",
    "slug": "my-first-post",
    "summary": "这是文章摘要",
    "content": "# 标题\n\n这是文章内容...",
    "coverImageUrl": "https://example.com/image.jpg",
    "published": true
  }'
```

### 编辑文章

#### 通过 Web 界面

1. 进入"后台管理"
2. 在文章列表中找到要编辑的文章
3. 点击"编辑"按钮
4. 修改文章内容
5. 点击"保存"

#### 通过 API

```bash
curl -X PUT http://localhost:8080/api/v1/admin/posts/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "更新后的标题",
    "content": "更新后的内容",
    "published": true
  }'
```

### 删除文章

#### 通过 Web 界面

1. 进入"后台管理"
2. 在文章列表中找到要删除的文章
3. 点击"删除"按钮
4. 确认删除

#### 通过 API

```bash
curl -X DELETE http://localhost:8080/api/v1/admin/posts/{id} \
  -H "Authorization: Bearer $TOKEN"
```

### Markdown 语法

文章内容支持完整的 Markdown 语法：

```markdown
# 一级标题
## 二级标题
### 三级标题

**粗体文本**
*斜体文本*
~~删除线~~

- 无序列表项 1
- 无序列表项 2

1. 有序列表项 1
2. 有序列表项 2

[链接文本](https://example.com)

![图片描述](https://example.com/image.jpg)

`行内代码`

​```kotlin
// 代码块
fun main() {
    println("Hello, World!")
}
​```

> 引用文本

---

| 表头1 | 表头2 |
|-------|-------|
| 内容1 | 内容2 |
```

---

## 👥 用户管理

### 注册新用户

#### 通过 Web 界面

1. 在登录页面点击"注册"
2. 填写注册信息：
   - 用户名
   - 邮箱
   - 密码
3. 点击"注册"

#### 通过 API

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 修改用户角色

只有管理员可以修改用户角色。

#### 通过 API

```bash
curl -X PUT http://localhost:8080/api/v1/admin/users/{id}/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"role": "ADMIN"}'
```

可用角色：
- `USER` - 普通用户（默认）
- `ADMIN` - 管理员

### 查看用户列表

只有管理员可以查看所有用户。

```bash
curl http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## ❓ 常见问题

### Q1: 如何修改管理员密码？

**方法一：通过数据库**

```bash
# 连接数据库
./db-manager.sh
# 选择 9 (连接数据库)

# 更新密码（密码会自动加密）
# 注意：需要使用 BCrypt 加密后的密码
```

**方法二：重新注册后提升权限**

```bash
# 1. 注册新账号
# 2. 使用数据库管理工具提升权限
UPDATE users SET role = 'ADMIN' WHERE email = 'newemail@example.com';
```

### Q2: 如何备份数据？

```bash
# 使用数据库管理工具
./db-manager.sh
# 选择 7 (备份数据库)

# 或手动备份
/opt/homebrew/opt/postgresql@15/bin/pg_dump personalblog > backup.sql
```

### Q3: 如何恢复数据？

```bash
# 使用数据库管理工具
./db-manager.sh
# 选择 8 (恢复数据库)

# 或手动恢复
/opt/homebrew/opt/postgresql@15/bin/psql personalblog < backup.sql
```

### Q4: 如何添加测试数据？

```bash
# 使用数据库管理工具
./db-manager.sh
# 选择 5 (创建测试数据)
```

### Q5: 如何清空所有数据？

```bash
# 使用数据库管理工具
./db-manager.sh
# 选择 6 (清空所有数据)
# 输入 yes 确认
```

### Q6: Android 应用无法连接后端？

检查以下几点：
1. 后端服务是否运行：`./status.sh`
2. API 地址是否正确：应为 `http://10.0.2.2:8080`
3. 网络权限是否配置：检查 AndroidManifest.xml
4. 网络安全策略是否配置：检查 network_security_config.xml

### Q7: 如何修改 API 地址？

编辑文件：`frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/data/remote/PlatformConfig.kt`

```kotlin
object ApiConfig {
    var apiBaseUrl: String = "http://your-domain.com"
}
```

### Q8: 如何部署到生产环境？

```bash
# 1. 构建前端
./gradlew :composeApp:wasmJsBrowserDistribution

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 3. 启动 Docker 服务
docker compose up -d
```

---

## 🔧 故障排除

### 后端服务无法启动

**症状**: 运行 `./gradlew :backend:run` 失败

**可能原因和解决方案**:

1. **PostgreSQL 未运行**
   ```bash
   brew services start postgresql@15
   ```

2. **数据库不存在**
   ```bash
   /opt/homebrew/opt/postgresql@15/bin/psql postgres -c "CREATE DATABASE personalblog;"
   ```

3. **端口被占用**
   ```bash
   # 查看占用 8080 端口的进程
   lsof -i :8080
   # 停止进程
   kill -9 <PID>
   ```

### Android 应用崩溃

**症状**: 应用启动后立即崩溃

**可能原因和解决方案**:

1. **网络权限未配置**
   - 检查 `AndroidManifest.xml` 是否包含 INTERNET 权限

2. **API 地址错误**
   - 确认使用 `http://10.0.2.2:8080` 而不是 `localhost`

3. **后端服务未运行**
   - 运行 `./status.sh` 检查服务状态

### 数据库连接失败

**症状**: 后端日志显示数据库连接错误

**可能原因和解决方案**:

1. **PostgreSQL 未运行**
   ```bash
   brew services start postgresql@15
   ```

2. **数据库用户不存在**
   ```bash
   /opt/homebrew/opt/postgresql@15/bin/psql postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'postgres';"
   ```

3. **数据库名称错误**
   - 确认数据库名为 `personalblog`

### API 响应 401 Unauthorized

**症状**: API 请求返回 401 错误

**可能原因和解决方案**:

1. **Token 过期**
   - 重新登录获取新 Token

2. **Token 格式错误**
   - 确认 Authorization header 格式：`Bearer <token>`

3. **用户权限不足**
   - 确认用户角色是否为 ADMIN（管理员接口）

### API 响应 500 Internal Server Error

**症状**: API 请求返回 500 错误

**可能原因和解决方案**:

1. **查看后端日志**
   ```bash
   tail -f /tmp/backend.log
   ```

2. **数据库查询错误**
   - 检查数据库表结构是否正确
   - 运行数据库迁移

3. **序列化错误**
   - 检查请求体格式是否正确

---

## 📞 获取帮助

### 文档资源

- **README.md** - 项目主文档
- **DEV_GUIDE.md** - 开发指南
- **FINAL_REPORT.md** - 完整项目报告
- **USER_MANUAL.md** - 用户手册（本文档）

### 工具命令

```bash
./status.sh          # 查看系统状态
./test-api.sh        # 测试 API
./db-manager.sh      # 管理数据库
```

### 日志文件

- 后端日志: `/tmp/backend.log`
- PostgreSQL 日志: 查看 Homebrew 服务日志

### 社区支持

- 提交 Issue
- 查看文档
- 运行测试工具

---

## 📋 附录

### API 端点列表

#### 公开接口

```
GET    /api/v1/posts              获取文章列表（分页）
GET    /api/v1/posts/{id}         获取文章详情
GET    /api/v1/posts/search       搜索文章
GET    /api/v1/tags               获取标签列表
GET    /api/v1/tags/{slug}        获取标签下的文章
GET    /api/v1/comments/{postId}  获取文章评论
```

#### 认证接口

```
POST   /api/v1/auth/register      用户注册
POST   /api/v1/auth/login         用户登录
```

#### 需要认证的接口

```
POST   /api/v1/comments           发表评论
```

#### 管理员接口

```
POST   /api/v1/admin/posts        创建文章
PUT    /api/v1/admin/posts/{id}   更新文章
DELETE /api/v1/admin/posts/{id}   删除文章
GET    /api/v1/admin/users        获取用户列表
PUT    /api/v1/admin/users/{id}/role  更新用户角色
```

### 快捷键（Web 应用）

- `Ctrl/Cmd + K` - 打开搜索
- `Ctrl/Cmd + /` - 切换主题
- `Esc` - 关闭对话框

### 系统要求

#### 开发环境

- JDK 17+
- Gradle 8.10+
- PostgreSQL 15+
- Android Studio (Android 开发)
- Xcode (iOS/macOS 开发，仅 macOS)

#### 生产环境

- Docker 20+
- Docker Compose 2+
- 2GB+ RAM
- 10GB+ 磁盘空间

---

**文档版本**: 1.0  
**最后更新**: 2026年2月20日  
**适用版本**: Personal Blog v1.0

🌟 **感谢使用 Personal Blog！**
