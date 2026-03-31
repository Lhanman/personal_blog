# Obsidian 博客导入脚本使用文档

## 脚本位置

- 脚本：`scripts/import_obsidian_blog.py`
- 默认文档目录：`/Users/lhanboyy/workbench/lhan_doc/个人项目/博客`

## 功能说明

该脚本用于将 Obsidian 中的博客 Markdown 文档导入到当前博客系统数据库中。

数据链路如下：

```text
Obsidian Markdown
    ↓ 读取本地目录
import_obsidian_blog.py
    ↓ 登录管理员接口
POST /api/v1/auth/login
    ↓ 创建 / 更新文章
POST /api/v1/admin/posts
PUT  /api/v1/admin/posts/{id}
    ↓
PostgreSQL / posts
```

## 使用前提

- 后端服务已启动，默认地址为 `http://localhost:9191`
- 数据库可用
- 管理员账号可登录
- Obsidian 文档目录存在

默认管理员账号：

- 邮箱：`admin@blog.com`
- 密码：`admin123`

## 命令格式

```bash
python3 scripts/import_obsidian_blog.py [options]
```

## 参数说明

```text
-h, --help
    显示帮助信息

--source-dir SOURCE_DIR
    指定 Markdown 文档目录

--base-url BASE_URL
    指定后端服务地址

--email EMAIL
    指定管理员邮箱

--password PASSWORD
    指定管理员密码

--dry-run
    只输出导入计划，不实际写入数据库

--update-existing
    如果 slug 已存在，则更新已有文章
```

## 常用示例

### 1. 预览导入计划

```bash
python3 scripts/import_obsidian_blog.py --dry-run
```

示例输出：

```text
Discovered 9 markdown notes in /Users/lhanboyy/workbench/lhan_doc/个人项目/博客
- personal-blog-project-overview <- 00 - 项目概览
- backend-architecture <- 01 - 后端架构
- frontend-architecture <- 02 - 前端架构
- database-design <- 03 - 数据库设计
- api-documentation <- 04 - API 文档
- user-manual <- 05 - 使用手册
- development-guide <- 06 - 开发指南
- deployment-guide <- 07 - 部署指南
- troubleshooting-guide <- 08 - 故障排除
```

### 2. 首次导入

```bash
python3 scripts/import_obsidian_blog.py
```

示例输出：

```text
CREATE personal-blog-project-overview -> id=10
CREATE backend-architecture -> id=11
CREATE frontend-architecture -> id=12
CREATE database-design -> id=13
CREATE api-documentation -> id=14
CREATE user-manual -> id=15
CREATE development-guide -> id=16
CREATE deployment-guide -> id=17
CREATE troubleshooting-guide -> id=18
Done. created=9, updated=0, skipped=0
```

### 3. 增量同步已有文章

```bash
python3 scripts/import_obsidian_blog.py --update-existing
```

示例输出：

```text
UPDATE personal-blog-project-overview -> id=10
UPDATE backend-architecture -> id=11
UPDATE frontend-architecture -> id=12
UPDATE database-design -> id=13
UPDATE api-documentation -> id=14
UPDATE user-manual -> id=15
UPDATE development-guide -> id=16
UPDATE deployment-guide -> id=17
UPDATE troubleshooting-guide -> id=18
Done. created=0, updated=9, skipped=0
```

### 4. 指定自定义目录和服务地址

```bash
python3 scripts/import_obsidian_blog.py \
  --source-dir '/path/to/obsidian/个人项目/博客' \
  --base-url 'http://localhost:8080' \
  --email 'admin@blog.com' \
  --password 'admin123'
```

## 输出说明

脚本运行时常见输出类型如下：

- `Discovered N markdown notes`：发现了多少篇 Markdown 文档
- `CREATE <slug> -> id=<id>`：成功新建文章
- `UPDATE <slug> -> id=<id>`：成功更新已有文章
- `SKIP <slug> (already exists)`：文章已存在且未启用更新模式
- `ERROR <slug> -> ...`：该文章导入失败
- `Done. created=X, updated=Y, skipped=Z`：本次执行汇总

## 当前内置映射

脚本会为以下文档使用固定 slug：

- `00 - 项目概览` → `personal-blog-project-overview`
- `01 - 后端架构` → `backend-architecture`
- `02 - 前端架构` → `frontend-architecture`
- `03 - 数据库设计` → `database-design`
- `04 - API 文档` → `api-documentation`
- `05 - 使用手册` → `user-manual`
- `06 - 开发指南` → `development-guide`
- `07 - 部署指南` → `deployment-guide`
- `08 - 故障排除` → `troubleshooting-guide`

## 注意事项

- 脚本默认会把文章设置为已发布：`published=true`
- 当前导入不附带标签，`tagIds` 默认为空
- 正文会在开头附加一行 Obsidian 来源标记
- 如果目标系统里已有相同 slug，且未传 `--update-existing`，则会跳过
- 如果新增了新的 Markdown 文件，需要在脚本中补充 slug 映射

## 故障排查

### 登录失败

检查：

- 后端地址是否正确
- 管理员邮箱和密码是否正确
- 后端服务是否已启动

### 导入失败

检查：

- 文档目录是否存在
- 文档名是否已在脚本中配置 slug 映射
- 后端管理员接口是否可访问

### 重复导入

如果只是想同步最新内容，请使用：

```bash
python3 scripts/import_obsidian_blog.py --update-existing
```
