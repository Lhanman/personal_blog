## MODIFIED Requirements

### Requirement: 博客文章列表展示
系统 SHALL 在首页展示所有已发布的博客文章列表，分为 Featured（精选）和 Recent（最新）两个分区，按发布时间倒序排列，支持分页加载。

#### Scenario: 首次加载文章列表
- **WHEN** 用户打开博客首页
- **THEN** 系统展示 Hero Section（博客简介）、Featured Section（精选文章）、Recent Section（最新文章），每页最多显示 10 篇

#### Scenario: 加载更多文章
- **WHEN** 用户滚动到列表底部或点击"加载更多"
- **THEN** 系统加载下一页文章并追加到 Recent Section 列表末尾

#### Scenario: 列表为空
- **WHEN** 数据库中没有已发布的文章
- **THEN** 系统在 Recent Section 显示 EmptyState 组件，提示"暂无文章"

### Requirement: 文章卡片信息展示
每篇文章在列表中 SHALL 以 PostCard 组件展示，Featured Section 使用 Featured 变体，Recent Section 使用 Default 变体，包含标题、摘要（可选）、发布日期、标签列表。

#### Scenario: Featured Section 文章卡片
- **WHEN** 文章出现在 Featured Section
- **THEN** 使用 PostCardVariant.Featured 渲染，显示完整摘要，字号较大

#### Scenario: Recent Section 文章卡片
- **WHEN** 文章出现在 Recent Section
- **THEN** 使用 PostCardVariant.Default 渲染，摘要最多 2 行

#### Scenario: 文章无封面图
- **WHEN** 文章未设置封面图
- **THEN** 卡片不显示图片区域，仅展示文字信息

#### Scenario: 点击文章卡片
- **WHEN** 用户点击任意文章卡片
- **THEN** 系统导航至该文章的阅读页

## ADDED Requirements

### Requirement: 首页 Hero Section
系统 SHALL 在首页顶部展示 HeroSection 组件，包含博客名称、简介和社交链接。

#### Scenario: 展示 Hero Section
- **WHEN** 用户打开博客首页
- **THEN** 页面顶部显示博客名称、一段简介文字、社交媒体图标链接行

### Requirement: 首页分区标题
系统 SHALL 使用 SectionTitle 组件在 Featured 和 Recent 分区前展示分区标题。

#### Scenario: 展示分区标题
- **WHEN** 首页渲染 Featured 或 Recent 分区
- **THEN** 分区顶部显示加粗标题（"Featured" 或 "Recent Posts"），下方有分隔线
