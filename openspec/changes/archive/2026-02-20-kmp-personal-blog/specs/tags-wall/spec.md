## ADDED Requirements

### Requirement: Tags 墙展示
系统 SHALL 在 Tags 页面展示所有已使用的标签，每个标签显示名称及对应的文章数量，按文章数量降序排列。

#### Scenario: 展示所有标签
- **WHEN** 用户进入 Tags 页面
- **THEN** 系统展示所有标签的云状或列表布局，每个标签附带文章计数

#### Scenario: 无标签
- **WHEN** 系统中没有任何已发布文章的标签
- **THEN** 显示"暂无标签"的空状态提示

### Requirement: 按标签筛选文章
系统 SHALL 支持用户点击某个标签后，展示所有包含该标签的文章列表。

#### Scenario: 点击标签跳转文章列表
- **WHEN** 用户在 Tags 页面点击某个标签
- **THEN** 系统导航至该标签的文章列表页，展示所有包含该标签的文章，按发布时间倒序

#### Scenario: 标签无文章
- **WHEN** 某标签下没有已发布的文章
- **THEN** 显示"该标签下暂无文章"的空状态提示

#### Scenario: URL 直接访问标签页
- **WHEN** 用户通过 URL（如 `/tags/kotlin`）直接访问某标签页
- **THEN** 系统正确展示该标签对应的文章列表
