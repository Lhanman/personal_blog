## ADDED Requirements

### Requirement: 首页整体布局结构
系统 SHALL 将首页划分为五个垂直分区：TopBar、HeroSection、Featured、Recent Posts、Footer，各分区之间使用品牌色分割线分隔。

#### Scenario: 首页完整渲染
- **WHEN** 用户打开博客首页
- **THEN** 页面从上到下依次显示：TopBar、HeroSection、Featured 分区、Recent Posts 分区、Footer

#### Scenario: 分区分割线展示
- **WHEN** 任意两个相邻分区之间需要分隔
- **THEN** 显示宽度 236dp、高度 2dp、颜色 #AB4B08 的水平分割线，居中对齐

### Requirement: 品牌色分割线组件
系统 SHALL 提供 `BrandDivider` 组件，用于首页各分区之间的视觉分隔，固定规格：宽 236dp、高 2dp、颜色 #AB4B08。

#### Scenario: 渲染品牌分割线
- **WHEN** `BrandDivider` 被渲染
- **THEN** 显示一条宽 236dp、高 2dp、颜色 #AB4B08 的水平线，在父容器中水平居中

### Requirement: 首页 Footer
系统 SHALL 在首页底部展示 Footer，显示版权信息 "Copyright © 2024 | All rights reserved."。

#### Scenario: 展示 Footer
- **WHEN** 用户滚动到首页底部
- **THEN** 显示版权文字 "Copyright © 2024 | All rights reserved."，文字居中，颜色使用次要文字色

### Requirement: 重设计 TopBar
系统 SHALL 重写顶部导航栏，左侧显示博客名称 Logo，右侧显示导航链接和主题切换按钮，参考 AstroPaper 风格。

#### Scenario: TopBar 基本展示
- **WHEN** 任意页面渲染
- **THEN** 顶部显示：左侧博客名称（可点击跳转首页）、右侧导航链接（Home、About、Tags、Search）、主题切换按钮

#### Scenario: 当前页面高亮
- **WHEN** 用户处于某个导航页面
- **THEN** 对应导航链接显示高亮或下划线样式，与其他链接区分

#### Scenario: 管理员额外入口
- **WHEN** 已登录用户角色为 ADMIN
- **THEN** 导航链接中额外显示 "Admin" 入口

#### Scenario: 登录/登出按钮
- **WHEN** 用户未登录
- **THEN** 右侧显示登录按钮；已登录时显示登出按钮
