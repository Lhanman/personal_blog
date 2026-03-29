## Why

当前顶部栏（AppTopBar）使用简单的 Row 布局，视觉风格与 AstroPaper 设计规范不符：Logo 颜色硬编码、导航项激活状态用下划线表示、缺少汉堡菜单（移动端）、整体间距和层次感不足。参考 Figma 设计稿（AstroPaper Community node-id=132-1163），需要将顶部栏升级为更贴近 AstroPaper 风格的实现。

## What Changes

- 重构 `AppTopBar` 组件，采用 AstroPaper 风格布局
- Logo 文字使用 `colorScheme.onBackground` 语义色，去除硬编码颜色
- 导航项激活状态改为底部高亮线（accent 色），去除下划线
- 导航链接在桌面端水平排列，移动端折叠为汉堡菜单（展开/收起）
- Search 导航项改为图标按钮（放大镜图标）
- 主题切换按钮保留，样式微调与整体风格统一
- 顶部栏与内容区之间的分隔线保留 `BrandDivider` 风格（橙色细线居中）
- 响应式：桌面端（≥768dp）完整导航，移动端汉堡菜单

## Capabilities

### New Capabilities

- `header-nav`：顶部导航栏组件，包含 Logo、导航链接、Search 图标、主题切换、登录/登出，支持响应式折叠

### Modified Capabilities

- `responsive-layout`：顶部栏需适配移动端汉堡菜单展开逻辑

## Impact

- 前端：`AppNavHost.kt`（AppTopBar 重构）、可能新增 `Header.kt` 组件文件
- 无后端变更、无共享模块变更、无部署变更
