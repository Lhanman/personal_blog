## Context

当前 `AppHeader` 在 `commonMain` 中统一渲染，desktop 入口 `frontend/composeApp/src/desktopMain/kotlin/com/personalblog/app/main.kt` 仅创建基础 `Window`，没有桌面端壳层能力。结果是：一方面文章详情、标签文章、管理编辑等层级页面缺少“返回上一页”入口；另一方面系统 titlebar 与应用 TopBar 分离，desktop 端顶部存在明显视觉断层。此次变更同时涉及 `desktopMain` 窗口入口、`commonMain` 导航元数据与顶部组件协作，属于跨模块 UI 壳层调整。

## Goals / Non-Goals

**Goals:**
- 为 desktop 端建立统一窗口壳层，承载沉浸式头部区域、返回入口与页面标题。
- 为层级页面定义稳定的返回策略，优先返回实际上一路径，其次回退到逻辑父页面。
- 保持现有 `NavHost`、ViewModel 与业务 Screen 结构基本不变，尽量将改动收敛在 shell/header 层。
- 保证深色/浅色主题下头部、窗口背景、分隔线与交互控件视觉一致。

**Non-Goals:**
- 不改动后端、shared DTO 或路由数据接口。
- 不重做移动端导航信息架构，只保持兼容。
- 不在本次变更中引入新的设计系统或第三方窗口管理库。

## Decisions

### 决策 1：引入路由元数据层，统一描述标题、层级与返回策略

**决策**：在 `commonMain` 为 `Screen` 补充 route metadata（如 `title`、`isTopLevel`、`parentRoute` 或等价映射），由 header/shell 基于当前 destination 决定显示 Logo、页面标题和返回按钮。

**理由**：当前代码只保存 route 字符串，无法稳定判断哪些页面应显示返回入口，也无法给 desktop 头部提供一致标题。将导航语义集中后，desktop 与其他平台都能共享同一份页面层级规则。

**替代方案**：在 `AppHeader` 中用 `startsWith`/硬编码 route 推断层级 → 易碎，后续新增页面时容易遗漏。

### 决策 2：desktop 平台通过专用 shell 注入窗口能力，commonMain 不使用 expect/actual

**决策**：在 `desktopMain` 增加桌面壳层入口（如 `DesktopWindowShell` 或等价包装），负责窗口拖拽区域、头部贴顶布局和窗口级状态；`commonMain` 仅消费抽象后的 header state / callbacks，不直接依赖桌面 API。

**理由**：此次能力只在 desktop 生效，直接使用 `desktopMain` 注入更简单，能避免为了单平台 UI 细节扩散 expect/actual。跨平台代码只感知“是否支持桌面壳层”和当前头部状态。

**替代方案**：为窗口行为建立 expect/actual → 对当前需求过重，增加维护成本。

### 决策 3：沉浸式头部采用单一视觉表面，交互控件与拖拽热区分离

**决策**：desktop 顶部使用统一背景与分隔线，将 titlebar/topbar 视为单一头部表面；非交互空白区作为拖拽热区，返回按钮、导航项、搜索、主题切换和窗口操作区保持独立点击区域。

**理由**：用户关注的是“titlebar 与内容视觉割裂”，统一表面可以先解决沉浸感问题；同时把拖拽区与按钮区分离，可以减少误触和窗口拖拽冲突。

**替代方案**：仅调整 `AppHeader` 配色和边距 → 能缓解割裂，但无法形成真正的 desktop 壳层体验。

### 决策 4：返回交互优先回退真实历史，其次降级到逻辑父路由

**决策**：点击返回按钮时优先执行 `navController.popBackStack()`；若当前页面通过冷启动、外链或刷新直接进入导致无可回退历史，则导航到 metadata 中定义的父级路由，并使用 `launchSingleTop` 避免重复堆叠。

**理由**：这同时覆盖“从列表进入详情”的自然返回与“直接进入详情页”的可恢复返回，交互更稳健。

**替代方案**：始终跳转固定父页面 → 会丢失真实浏览链路；仅依赖 `popBackStack()` → 冷启动详情页无返回效果。

## Risks / Trade-offs

- **[风险] desktop 窗口拖拽区与按钮命中冲突** → **缓解**：限制拖拽区只覆盖空白区域，交互组件单独包裹不可拖拽容器。
- **[风险] 路由 metadata 与实际 NavHost 不一致** → **缓解**：将 metadata 与 `Screen` 集中维护，并在实现阶段补充针对标题/返回逻辑的单元测试。
- **[取舍] header 会引入桌面分支逻辑** → **缓解**：将 desktop 专属视觉与窗口行为下沉到 shell，header 只处理展示状态。
- **[取舍] 冷启动降级返回可能与用户预期“上一页”不完全一致** → **缓解**：在 spec 中明确优先真实历史、其次逻辑父页，确保行为可预测。
