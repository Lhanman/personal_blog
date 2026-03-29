## MODIFIED Requirements

### Requirement: 博客文章列表展示
系统 SHALL 在首页展示已发布的博客文章列表，分为 Featured（精选）和 Recent Posts（最新）两个分区，Featured 固定展示最多 3 篇，Recent Posts 固定展示最新 3 篇，底部提供 "All Posts →" 跳转入口。

#### Scenario: 首次加载文章列表
- **WHEN** 用户打开博客首页
- **THEN** 系统展示 HeroSection、Featured Section（最多 3 篇精选文章）、Recent Posts Section（最新 3 篇文章）、Footer

#### Scenario: Featured 分区展示 3 篇
- **WHEN** 数据库中有带 "featured" 标签的文章
- **THEN** Featured 分区最多展示 3 篇，超出部分不显示

#### Scenario: Recent Posts 展示 3 篇并提供跳转
- **WHEN** 首页渲染 Recent Posts 分区
- **THEN** 展示最新 3 篇非精选文章，分区底部居中显示 "All Posts →" 文字按钮，点击跳转到完整文章列表页

#### Scenario: 列表为空
- **WHEN** 数据库中没有已发布的文章
- **THEN** 系统在 Recent Posts Section 显示 EmptyState 组件，提示"暂无文章"
