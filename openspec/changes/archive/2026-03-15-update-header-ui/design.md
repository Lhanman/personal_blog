## Context

当前 `AppTopBar` 实现在 `AppNavHost.kt` 中，是一个简单的 `Row` 布局：Logo 颜色硬编码为 `#EAEDF3`，导航激活状态用下划线表示，没有移动端汉堡菜单，整体与 AstroPaper Figma 设计规范不符。

参考 AstroPaper 设计（Figma node-id=132-1163）：
- 桌面端：Logo 左对齐，导航链接右对齐，Search 图标 + 主题切换按钮
- 移动端：Logo 左对齐，汉堡菜单图标右对齐，点击展开垂直导航列表
- 激活状态：底部高亮线（accent 色 `#FF6B01`），无下划线

## Goals / Non-Goals

**Goals:**
- 重构 `AppTopBar` 为 AstroPaper 风格，使用语义化颜色
- 支持响应式：桌面端水平导航，移动端汉堡菜单折叠
- Search 导航项改为图标按钮
- 激活状态改为底部高亮线

**Non-Goals:**
- 不修改后端或共享模块
- 不引入新的第三方依赖
- 不修改其他页面组件

## Decisions

### 决策 1：将 AppTopBar 提取为独立组件文件

**决策**：将 `AppTopBar` 从 `AppNavHost.kt` 中提取到 `components/Header.kt`。

**理由**：`AppNavHost.kt` 已经较长，Header 逻辑独立后更易维护和测试。

**替代方案**：保留在 `AppNavHost.kt` 中 → 文件过长，职责不单一。

---

### 决策 2：响应式折叠使用 `BoxWithConstraints` + `remember` 状态

**决策**：用 `BoxWithConstraints` 检测宽度（768dp 断点），移动端用 `var menuExpanded by remember { mutableStateOf(false) }` 控制汉堡菜单展开。

**理由**：与项目现有响应式方案（`ResponsiveLayout.kt`）保持一致，无需引入新机制。

**替代方案**：平台特定 `expect/actual` → 过度复杂，此处纯 UI 逻辑无需平台差异。

---

### 决策 3：激活状态用 `Box` + 底部 2dp 高亮线

**决策**：`NavItem` 激活时在文字下方叠加一个 `height=2.dp`、`color=accent(#FF6B01)` 的 `Box`，使用 `Box` 包裹实现。

**理由**：AstroPaper 原版设计即为底部高亮线，视觉更简洁，符合设计规范。

**替代方案**：继续用 `TextDecoration.Underline` → 与设计稿不符，且无法控制颜色和粗细。

---

### 决策 4：主题适配使用语义化颜色

**决策**：Logo 和导航文字使用 `MaterialTheme.colorScheme.onBackground`，激活色使用硬编码 `Color(0xFFFF6B01)`（accent 色，深浅模式均适用）。

**理由**：`AppColorScheme.kt` 未定义 `tertiary`/`accent` 语义色，直接使用 accent 色硬编码是项目现有惯例（`BrandDivider` 也使用 `#AB4B08`）。

**替代方案**：在 `AppColorScheme.kt` 新增 accent 颜色 token → 超出本次变更范围。

## Risks / Trade-offs

- **[风险] 汉堡菜单状态在导航后不自动关闭** → 缓解：在 `NavItem` 的 `onClick` 中同时调用 `menuExpanded = false`
- **[风险] WASM/Web 平台 `BoxWithConstraints` 宽度获取时机** → 缓解：与现有 `ResponsiveLayout` 使用相同方式，已验证可用
- **[取舍] 提取为独立文件增加一个文件** → 可接受，职责更清晰
