## ADDED Requirements

### Requirement: Markdown 文章渲染
系统 SHALL 将文章的 Markdown 内容解析并渲染为格式化的富文本，支持标题、段落、代码块、引用、列表、图片、链接等标准 Markdown 语法。

#### Scenario: 渲染标准 Markdown 内容
- **WHEN** 用户打开一篇文章
- **THEN** 系统将 Markdown 解析为可读的格式化内容，代码块带语法高亮

#### Scenario: 渲染内嵌图片
- **WHEN** 文章 Markdown 中包含图片链接
- **THEN** 系统异步加载并展示图片，加载中显示占位符

#### Scenario: 文章不存在
- **WHEN** 用户访问一个不存在的文章 ID
- **THEN** 系统显示 404 提示页面

### Requirement: 文章目录导航
系统 SHALL 根据文章中的标题（H1-H3）自动生成目录，并在阅读页侧边栏展示，支持点击跳转。

#### Scenario: 文章含多级标题
- **WHEN** 文章包含 H1、H2、H3 标题
- **THEN** 侧边栏展示层级缩进的目录，当前阅读位置对应的目录项高亮

#### Scenario: 点击目录项
- **WHEN** 用户点击目录中的某个标题
- **THEN** 页面平滑滚动至对应标题位置

#### Scenario: 文章无标题
- **WHEN** 文章内容不含任何标题
- **THEN** 不显示目录侧边栏

### Requirement: 文章元信息展示
系统 SHALL 在文章顶部展示标题、作者、发布日期、更新日期（若有）、标签列表、预计阅读时长。

#### Scenario: 展示文章元信息
- **WHEN** 用户打开文章阅读页
- **THEN** 页面顶部显示完整的元信息区域，标签可点击跳转至对应标签筛选页
