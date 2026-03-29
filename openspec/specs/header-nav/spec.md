# Header Navigation Spec

## Purpose

定义顶部导航栏的布局、响应式行为、激活状态、主题适配及权限控制要求。

## Requirements

### Requirement: 顶部导航栏布局
顶部导航栏 SHALL 包含 Logo、导航链接区、工具栏区（Search 图标、主题切换、登录/登出），并在桌面端（≥768dp）水平排列，在移动端（<768dp）折叠为汉堡菜单。

#### Scenario: 桌面端完整导航显示
- **WHEN** 视口宽度 ≥ 768dp
- **THEN** Logo 左对齐，导航链接（Home、About、Tags）水平排列于右侧，Search 图标、主题切换按钮、登录/登出按钮依次排列

#### Scenario: 移动端汉堡菜单折叠
- **WHEN** 视口宽度 < 768dp
- **THEN** 仅显示 Logo 和汉堡菜单图标，导航链接隐藏

#### Scenario: 移动端汉堡菜单展开
- **WHEN** 用户点击汉堡菜单图标
- **THEN** 导航链接垂直展开显示在顶部栏下方，汉堡图标变为关闭图标

#### Scenario: 移动端导航后菜单关闭
- **WHEN** 用户点击任意导航链接
- **THEN** 菜单自动收起，导航至目标页面

### Requirement: 导航激活状态指示
激活的导航项 SHALL 在文字下方显示 2dp 高的橙色（#FF6B01）高亮线，文字颜色使用 accent 色，不使用下划线装饰。

#### Scenario: 当前页面导航项高亮
- **WHEN** 当前路由与导航项路由匹配
- **THEN** 该导航项文字颜色为 #FF6B01，文字下方显示 2dp 橙色高亮线

#### Scenario: 非激活导航项样式
- **WHEN** 导航项路由与当前路由不匹配
- **THEN** 文字颜色为 `colorScheme.onBackground`，无高亮线

### Requirement: Logo 语义化颜色
Logo 文字 SHALL 使用 `MaterialTheme.colorScheme.onBackground` 颜色，不硬编码颜色值，支持深色/浅色主题自动切换。

#### Scenario: 浅色主题 Logo 颜色
- **WHEN** 当前主题为浅色模式
- **THEN** Logo 文字颜色为 `LightColorScheme.onBackground`（#1E293B）

#### Scenario: 深色主题 Logo 颜色
- **WHEN** 当前主题为深色模式
- **THEN** Logo 文字颜色为 `DarkColorScheme.onBackground`（#E2E8F0）

### Requirement: Search 图标按钮
Search 导航项 SHALL 以图标按钮形式呈现（放大镜图标），而非文字链接，点击后导航至搜索页。

#### Scenario: Search 图标点击导航
- **WHEN** 用户点击 Search 图标按钮
- **THEN** 应用导航至搜索页（Screen.Search.route）

#### Scenario: Search 图标激活状态
- **WHEN** 当前路由为搜索页
- **THEN** Search 图标颜色变为 accent 色（#FF6B01）

### Requirement: Admin 导航项条件显示
Admin 导航项 SHALL 仅在用户角色为 ADMIN 时显示，普通用户和未登录用户不可见。

#### Scenario: ADMIN 用户显示 Admin 入口
- **WHEN** 用户已登录且角色为 ADMIN
- **THEN** 导航栏显示 Admin 链接

#### Scenario: 非 ADMIN 用户隐藏 Admin 入口
- **WHEN** 用户未登录或角色非 ADMIN
- **THEN** 导航栏不显示 Admin 链接
