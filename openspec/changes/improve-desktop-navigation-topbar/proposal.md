## Why

当前 desktop 版沿用通用顶部导航，页面进入详情后缺少返回上一页的明确入口；同时系统标题栏与应用 TopBar 分离，导致桌面端视觉割裂、沉浸感不足。现在需要补齐桌面端导航层级与窗口外观规范，提升可用性与一致性。

## What Changes

- 为 desktop 端新增窗口级导航壳层，统一管理页面标题、返回按钮与窗口拖拽区。
- 为存在层级关系的页面提供“返回上一页”交互，并定义何时显示/隐藏返回入口。
- 重构 desktop 顶部栏，使 TopBar 与 titlebar 视觉融合，形成沉浸式头部区域。
- 保持现有跨平台导航结构可复用，避免影响 Android、iOS、WASM 的既有体验。

## Capabilities

### New Capabilities
- `desktop-window-shell`: 定义 desktop 端窗口级壳层、沉浸式 titlebar/topbar 融合样式与返回交互规范。

### Modified Capabilities
- `header-nav`: 调整顶部导航在 desktop 场景下的层级展示、标题呈现与返回入口协同行为。

## Impact

- 前端：主要影响 `frontend/composeApp/src/desktopMain/`、`frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/navigation/`、`frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/`
- 后端/共享模块/部署：无变更
- 依赖：预计不新增第三方依赖，优先复用 Compose Desktop 与现有 Material3 组件
