## Why

当前首页的顶部栏样式简陋，Hero Section 信息不够突出，Featured/Recent 分区缺乏视觉层次感，整体 UI 与 AstroPaper 风格相差较大。需要重新设计首页以提升视觉品质和个人品牌感。

## What Changes

- **重写 TopBar**：参考 AstroPaper 风格，左侧显示博客名称 Logo，右侧显示导航链接（Home、About、Tags、Search）+ 主题切换按钮
- **重写 HeroSection**：展示网站名称 "LhanBoyy"（48sp，颜色 #EAEDF3，字间距 0.03em）+ 个人介绍文字
- **Featured 分区**：仅展示 3 篇精选文章，使用 Featured 变体 PostCard
- **Recent Posts 分区**：展示最新 3 篇文章，底部居中显示 "All Posts →" 跳转链接
- **Footer**：底部显示 "Copyright © 2024 | All rights reserved."
- **分割线样式**：各分区之间使用固定宽度 236dp、高度 2dp、颜色 #AB4B08 的分割线

## Capabilities

### New Capabilities

- `homepage-layout`: 首页整体布局重设计，包含 TopBar、HeroSection、Featured、Recent Posts、Footer 五个分区及分割线样式

### Modified Capabilities

- `blog-list`: Featured 分区固定展示 3 篇，Recent Posts 固定展示 3 篇并增加 "All Posts →" 跳转入口
- `blog-ui-components`: HeroSection 组件更新为新的视觉规格（字号、颜色、字间距），SectionTitle 分割线改为品牌色 #AB4B08

## Impact

- 前端：`AppNavHost.kt`（TopBar 重写）、`BlogListScreen.kt`（首页布局）、`HeroSection.kt`（组件更新）、`SectionTitle.kt`（分割线样式）
- 新增：`Footer.kt` 组件
- 不影响后端、共享模块、部署配置
