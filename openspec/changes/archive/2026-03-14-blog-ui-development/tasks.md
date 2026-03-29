## 1. 原子组件实现

- [x] 1.1 重构 `TagChip.kt`，统一标签样式（圆角、背景色、字号），支持深色/浅色主题
- [x] 1.2 新增 `SectionTitle.kt`，展示分区标题 + 下方分隔线
- [x] 1.3 新增 `EmptyState.kt`，居中展示图标和提示文字

## 2. PostCard 组件实现

- [x] 2.1 定义 `PostCardVariant` 枚举（Default、Compact、Featured）
- [x] 2.2 实现 `PostCard.kt`，支持三种变体，移除 `@Deprecated` 标记
- [x] 2.3 验证 PostCard 在深色/浅色主题下的渲染效果

## 3. HeroSection 组件实现

- [x] 3.1 新增 `HeroSection.kt`，展示博客名称、简介文字、社交链接行
- [x] 3.2 社交链接支持空状态（无链接时不渲染链接行）

## 4. 响应式布局工具

- [x] 4.1 新增 `ResponsiveLayout.kt`，封装 `BoxWithConstraints` 断点逻辑（768dp）
- [x] 4.2 新增 `ContentContainer.kt`，桌面端限制内容最大宽度 768dp 并居中

## 5. BlogListScreen 重构

- [x] 5.1 重构 `BlogListScreen.kt`，移除 `@Deprecated`，实现 Hero + Featured + Recent 三分区布局
- [x] 5.2 Featured Section 使用 `PostCardVariant.Featured`，Recent Section 使用 `PostCardVariant.Default`
- [x] 5.3 Recent Section 底部添加"加载更多"按钮，触发 ViewModel 分页加载
- [x] 5.4 列表为空时展示 `EmptyState` 组件
- [x] 5.5 适配移动端（单列）和桌面端（居中内容区）布局

## 6. BlogReaderScreen 重构

- [x] 6.1 重构 `BlogReaderScreen.kt`，移除 `@Deprecated`，实现文章元信息 + 正文 + 评论布局
- [x] 6.2 文章元信息区展示标题、发布日期、标签列表（可点击）、预计阅读时长
- [x] 6.3 桌面端展示 `TocSidebar` 侧边栏，移动端隐藏目录
- [x] 6.4 正文使用现有 `MarkdownContent` 组件渲染
- [x] 6.5 底部展示评论区（复用现有 `CommentInput` 和 `CommentItem`）

## 7. SearchScreen 重构

- [x] 7.1 重构 `SearchScreen.kt`，移除 `@Deprecated`，实现搜索栏 + 结果列表布局
- [x] 7.2 搜索栏样式对齐 AstroPaper 设计（圆角边框、搜索图标、占位文字）
- [x] 7.3 搜索结果使用 `PostCardVariant.Compact` 渲染，关键词高亮复用 `HighlightedText`
- [x] 7.4 无结果时展示 `EmptyState` 组件
- [x] 7.5 搜索框为空时展示初始空状态（不显示结果区域）

## 8. TagsWallScreen 重构

- [x] 8.1 重构 `TagsWallScreen.kt`，移除 `@Deprecated`，实现标签云布局
- [x] 8.2 每个标签使用 `TagChip` 展示名称和文章数量
- [x] 8.3 无标签时展示 `EmptyState` 组件

## 9. TagPostsScreen 重构

- [x] 9.1 重构 `TagPostsScreen.kt`，移除 `@Deprecated`，实现标签文章列表布局
- [x] 9.2 页面顶部展示当前标签名称（使用 `SectionTitle`）
- [x] 9.3 文章列表使用 `PostCardVariant.Default` 渲染
- [x] 9.4 无文章时展示 `EmptyState` 组件

## 10. 集成验证

- [x] 10.1 运行前端编译，确认无编译错误（`./gradlew :composeApp:wasmJsBrowserDistribution`）
- [x] 10.2 验证所有页面在深色/浅色主题切换时正常渲染
- [x] 10.3 验证移动端和桌面端响应式布局断点切换正常
- [x] 10.4 运行前端单元测试（`./gradlew :composeApp:test`）
