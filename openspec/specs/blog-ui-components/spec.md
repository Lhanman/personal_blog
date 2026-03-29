## ADDED Requirements

### Requirement: PostCard 组件
系统 SHALL 提供 `PostCard` 可复用组件，支持 Default、Compact、Featured 三种变体，展示文章标题、发布日期、标签、摘要（可选）。

#### Scenario: Default 变体展示
- **WHEN** 使用 `PostCardVariant.Default` 渲染文章卡片
- **THEN** 卡片显示发布日期、标签列表、标题（最多 2 行）、摘要（最多 2 行）

#### Scenario: Compact 变体展示
- **WHEN** 使用 `PostCardVariant.Compact` 渲染文章卡片
- **THEN** 卡片仅显示发布日期、标签列表、标题（最多 1 行），不显示摘要

#### Scenario: Featured 变体展示
- **WHEN** 使用 `PostCardVariant.Featured` 渲染文章卡片
- **THEN** 卡片显示发布日期、标签列表、标题（最多 2 行）、完整摘要（最多 3 行），字号略大

#### Scenario: 点击卡片
- **WHEN** 用户点击任意变体的文章卡片
- **THEN** 触发 `onClick` 回调，导航至文章详情页

#### Scenario: 深色主题适配
- **WHEN** 系统处于深色主题
- **THEN** 卡片文字和背景颜色自动切换为深色配色，对比度符合可读性要求

### Requirement: HeroSection 组件
系统 SHALL 提供 `HeroSection` 可复用组件，展示博客名称、个人介绍文字，使用指定的视觉规格。

#### Scenario: 展示博客基本信息
- **WHEN** `HeroSection` 被渲染
- **THEN** 显示博客名称 "LhanBoyy"（字号 48sp，颜色 #EAEDF3，字间距 0.03em）和个人介绍文字（"我是 LhanBoyy，该博客用 Kotlin Multiplatform 编写，用于对技术和生活的小记录"）

#### Scenario: 深色主题适配
- **WHEN** 系统处于深色主题
- **THEN** 博客名称颜色保持 #EAEDF3，介绍文字使用次要文字色

#### Scenario: 社交链接为空
- **WHEN** 未配置任何社交链接
- **THEN** 不显示社交链接行，其他内容正常展示

### Requirement: SectionTitle 组件
系统 SHALL 提供 `SectionTitle` 组件，统一各页面分区标题的样式，分割线使用品牌色。

#### Scenario: 渲染分区标题
- **WHEN** `SectionTitle` 被渲染并传入标题文字
- **THEN** 显示加粗的分区标题，下方有宽 236dp、高 2dp、颜色 #AB4B08 的品牌色分割线

### Requirement: EmptyState 组件
系统 SHALL 提供 `EmptyState` 组件，在列表为空时展示统一的空状态提示。

#### Scenario: 展示空状态
- **WHEN** `EmptyState` 被渲染并传入提示文字
- **THEN** 居中显示图标和提示文字，样式与整体主题一致
