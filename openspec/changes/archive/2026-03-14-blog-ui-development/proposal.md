## Why

当前项目的前端 UI 组件（`PostCard`、`BlogListScreen` 等）已被标记为 `@Deprecated`，等待基于设计稿的实现。项目已有 AstroPaper 风格的配色方案和字体系统，但核心页面和组件仍使用占位符。需要根据 AstroPaper 设计风格完成博客列表、文章详情、搜索、标签等核心页面的 UI 开发，提供完整的用户体验。

## What Changes

- 实现 `PostCard` 组件，支持多种布局变体（Featured/Recent/Search Result）
- 重构 `BlogListScreen`，包含 Hero Section、Featured Section、Recent Section
- 重构 `BlogReaderScreen`，实现文章详情页布局（标题、元信息、目录、正文、评论）
- 重构 `SearchScreen`，实现搜索栏、结果列表、空状态
- 重构 `TagsWallScreen` 和 `TagPostsScreen`，实现标签墙和标签文章列表
- 优化 `TagChip` 组件，统一标签样式
- 新增 `HeroSection`、`SectionTitle`、`EmptyState` 等可复用组件
- 适配深色/浅色主题，遵循现有 `AppColorScheme` 和 `AppTypography`

## Capabilities

### New Capabilities
- `blog-ui-components`: 博客核心 UI 组件库（PostCard、HeroSection、SectionTitle、EmptyState）
- `responsive-layout`: 响应式布局系统（适配桌面/移动端）

### Modified Capabilities
- `blog-list`: 更新首页布局需求，增加 Hero Section 和 Featured/Recent 分区
- `blog-reader`: 更新文章详情页布局需求，增加目录侧边栏和元信息展示
- `blog-search`: 更新搜索页布局需求，增加搜索栏样式和结果卡片
- `tags-wall`: 更新标签墙布局需求，增加标签卡片样式

## Impact

**前端代码**
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/`
  - 移除 `@Deprecated` 标记，重新实现 `PostCard.kt`
  - 新增 `HeroSection.kt`、`SectionTitle.kt`、`EmptyState.kt`
  - 优化 `TagChip.kt`
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/screen/`
  - 重构 `BlogListScreen.kt`、`BlogReaderScreen.kt`、`SearchScreen.kt`
  - 重构 `TagsWallScreen.kt`、`TagPostsScreen.kt`

**主题系统**
- 复用现有 `AppColorScheme.kt` 和 `AppTypography.kt`
- 确保所有组件支持深色/浅色主题切换

**依赖**
- 无新增外部依赖，使用现有 Compose Multiplatform 和 Material3
