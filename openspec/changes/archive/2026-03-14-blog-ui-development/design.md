## Context

当前项目使用 Compose Multiplatform 构建跨平台博客前端，已有完整的 ViewModel 层、数据层和导航系统，但核心 UI 组件和页面被标记为 `@Deprecated`，使用占位符实现。项目已配置 AstroPaper 风格的配色方案（`AppColorScheme.kt`）和字体系统（`AppTypography.kt`，使用 Noto Sans SC），支持深色/浅色主题切换。

现有技术栈：
- Compose Multiplatform (Android/iOS/macOS/Web)
- Material3 设计系统
- Jetpack Navigation Compose
- MVVM 架构（ViewModel + Repository + RemoteDataSource）

约束条件：
- 不引入新的外部依赖
- 复用现有主题系统和字体
- 保持 ViewModel 接口不变
- 支持所有平台（Android/iOS/macOS/Web）

## Goals / Non-Goals

**Goals:**
- 实现 AstroPaper 风格的博客 UI，包含首页、文章详情、搜索、标签页
- 提供可复用的组件库（PostCard、HeroSection、SectionTitle、EmptyState）
- 支持响应式布局，适配桌面和移动端
- 完整支持深色/浅色主题切换
- 保持代码简洁，避免过度抽象

**Non-Goals:**
- 不修改 ViewModel 层和数据层逻辑
- 不实现动画效果（后续迭代）
- 不支持自定义主题配置（使用固定配色）
- 不实现图片懒加载和缓存（使用 Compose 默认行为）

## Decisions

### 1. 组件层级设计

**决策：采用三层组件架构**
- **原子组件**：`TagChip`、`EmptyState`、`SectionTitle`（单一职责，高复用）
- **分子组件**：`PostCard`、`HeroSection`（组合原子组件，业务相关）
- **页面组件**：`BlogListScreen`、`BlogReaderScreen`（组合分子组件，完整页面）

**理由：**
- 符合 Atomic Design 原则，便于维护和测试
- 避免组件嵌套过深，保持代码可读性
- 原子组件可在多个页面复用

**替代方案：**
- 扁平化组件结构（所有组件平级）→ 难以管理，复用性差
- 更细粒度的拆分（如 PostCardTitle、PostCardMeta）→ 过度抽象，增加复杂度

### 2. PostCard 变体设计

**决策：使用单一 `PostCard` 组件 + 参数控制变体**

```kotlin
@Composable
fun PostCard(
    post: PostSummary,
    variant: PostCardVariant = PostCardVariant.Default,
    onClick: () -> Unit
)

enum class PostCardVariant {
    Default,    // 标准卡片（标题 + 描述 + 标签）
    Compact,    // 紧凑卡片（仅标题 + 标签）
    Featured    // 特色卡片（大标题 + 完整描述）
}
```

**理由：**
- 避免创建多个相似组件（`FeaturedPostCard`、`CompactPostCard`）
- 通过枚举明确变体类型，便于扩展
- 内部使用 `when` 语句控制布局差异

**替代方案：**
- 为每种变体创建独立组件 → 代码重复，难以维护
- 使用布尔参数（`isCompact`、`isFeatured`）→ 参数组合爆炸，语义不清

### 3. 响应式布局策略

**决策：使用 `BoxWithConstraints` + 断点判断**

```kotlin
@Composable
fun BlogListScreen(...) {
    BoxWithConstraints {
        val isMobile = maxWidth < 768.dp
        if (isMobile) {
            MobileLayout()
        } else {
            DesktopLayout()
        }
    }
}
```

**理由：**
- Compose Multiplatform 原生支持，无需额外依赖
- 断点清晰（768dp 为移动/桌面分界线）
- 可根据平台特性调整布局（如 iOS 使用 Safe Area）

**替代方案：**
- 使用 `LocalConfiguration` → 仅 Android 可用，不跨平台
- 使用第三方响应式库 → 增加依赖，学习成本高

### 4. 主题适配方案

**决策：使用 Material3 语义化颜色 + 自定义扩展**

```kotlin
// 使用 Material3 语义化颜色
Text(
    text = post.title,
    color = MaterialTheme.colorScheme.onSurface
)

// 自定义扩展颜色（如需）
val ColorScheme.accent: Color
    @Composable get() = if (isSystemInDarkMode()) Color(0xFF60A5FA) else Color(0xFF3B82F6)
```

**理由：**
- 自动适配深色/浅色主题，无需手动判断
- 符合 Material Design 规范，语义清晰
- 扩展颜色仅在必要时添加，避免过度定制

**替代方案：**
- 手动判断 `isSystemInDarkMode()` → 代码冗余，易出错
- 完全自定义颜色系统 → 脱离 Material3 生态，维护成本高

### 5. 文章详情页目录实现

**决策：使用 `LazyColumn` + 锚点滚动**

```kotlin
@Composable
fun BlogReaderScreen(...) {
    val listState = rememberLazyListState()
    Row {
        TocSidebar(
            headings = viewModel.headings,
            onHeadingClick = { index ->
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                }
            }
        )
        LazyColumn(state = listState) {
            items(viewModel.contentBlocks) { block ->
                MarkdownContent(block)
            }
        }
    }
}
```

**理由：**
- 复用现有 `TocSidebar` 组件
- `LazyColumn` 支持大文档性能优化
- 锚点滚动体验流畅

**替代方案：**
- 使用 `Column` + `Modifier.verticalScroll()` → 大文档性能差
- 使用 HTML 锚点（Web 平台）→ 不跨平台

## Risks / Trade-offs

### 1. 响应式布局复杂度
**风险：** 移动端和桌面端布局差异大，可能导致代码分支过多。
**缓解：** 提取共享布局逻辑到独立函数，仅在必要时分支。使用 `@Preview` 验证两端布局。

### 2. PostCard 变体扩展性
**风险：** 未来可能需要更多变体（如带封面图的卡片），枚举方式可能不够灵活。
**缓解：** 当前设计支持 3 种变体已满足需求。如需扩展，可重构为 `PostCardStyle` 数据类，支持更细粒度配置。

### 3. 跨平台字体渲染差异
**风险：** Noto Sans SC 在不同平台渲染效果可能不一致（如 iOS 字重偏细）。
**缓解：** 使用 `@Preview` 在各平台测试。如有问题，可针对平台调整 `fontWeight`（通过 `expect/actual`）。

### 4. 深色主题对比度
**风险：** AstroPaper 配色在深色模式下对比度可能不足（如 `DarkSecondary` 与 `DarkBackground`）。
**缓解：** 使用 Material3 的 `surfaceVariant` 和 `outline` 颜色增强层次感。必要时调整 `AppColorScheme.kt` 中的颜色值。

### 5. 大文档渲染性能
**风险：** 文章详情页包含大量 Markdown 内容时，`LazyColumn` 可能卡顿。
**缓解：** 使用 `MarkdownContent` 组件的懒加载特性。如仍有问题，考虑分页加载或虚拟滚动。
