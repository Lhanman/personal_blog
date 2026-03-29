## Context

当前首页 TopBar 样式简陋（纯文字按钮行），HeroSection 缺乏视觉冲击力，Featured/Recent 分区没有明确的数量限制，整体与 AstroPaper 风格相差较大。本次重设计聚焦前端 UI 层，不涉及后端或共享模块变更。

现有相关文件：
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/navigation/AppNavHost.kt` — TopBar 当前实现
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/screen/BlogListScreen.kt` — 首页布局
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/HeroSection.kt` — Hero 组件
- `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/SectionTitle.kt` — 分区标题组件

## Goals / Non-Goals

**Goals:**
- 重写 `AppTopBar`：左侧 Logo 文字，右侧导航链接 + 主题切换 + 登录/登出
- 更新 `HeroSection`：博客名 48sp / #EAEDF3 / letterSpacing 0.03em + 个人介绍文字
- 更新 `SectionTitle`：分割线改为品牌色 #AB4B08，宽 236dp，高 2dp
- 新增 `BrandDivider` 原子组件：固定规格品牌色分割线，供各分区复用
- 新增 `Footer` 组件：版权文字居中
- 重写 `BlogListScreen`：Featured 固定 3 篇，Recent Posts 固定 3 篇 + "All Posts →" 跳转

**Non-Goals:**
- 不修改后端 API
- 不修改共享 DTO
- 不修改其他页面（SearchScreen、BlogReaderScreen 等）
- 不实现完整文章列表页（"All Posts" 目标页可复用现有 BlogListScreen 或留空）

## Decisions

### Decision 1：BrandDivider 作为独立原子组件

**决策**：新建 `BrandDivider.kt`，而非在每处内联硬编码分割线。

**理由**：分割线规格（236dp × 2dp × #AB4B08）在首页多处复用（HeroSection 下方、Featured 下方、Recent Posts 下方、Footer 上方），提取为组件避免重复，且后续修改只需改一处。

**替代方案**：在 `SectionTitle` 内部直接硬编码 → 无法在 HeroSection 和 Footer 处复用，放弃。

### Decision 2：颜色 #AB4B08 / #EAEDF3 直接使用硬编码常量

**决策**：在 `BrandDivider` 和 `HeroSection` 中使用 `Color(0xFFAB4B08)` / `Color(0xFFEAEDF3)` 硬编码，不扩展 Material3 colorScheme。

**理由**：这两个颜色是品牌固定色，不随深色/浅色主题切换变化（#EAEDF3 在深色背景下作为主标题色，#AB4B08 是品牌强调色）。扩展 colorScheme 需修改主题文件，增加复杂度，收益不大。

**替代方案**：在 `AppColorScheme.kt` 中添加自定义颜色 token → 更规范但过度工程化，放弃。

### Decision 3：Recent Posts "All Posts →" 使用 TextButton 导航

**决策**：在 Recent Posts 分区底部添加居中 `TextButton`，点击触发 `onAllPostsClick` 回调，由 `AppNavHost` 决定导航目标。

**理由**：保持 Screen 层无导航逻辑，符合现有 MVVM 架构约定。

**替代方案**：直接在 Screen 内调用 navController → 破坏分层，放弃。

### Decision 4：TopBar 当前页面高亮通过 NavController 当前路由判断

**决策**：在 `AppTopBar` 中通过 `navController.currentBackStackEntryAsState()` 获取当前路由，对匹配的导航项添加下划线或颜色高亮。

**理由**：Navigation Compose 已提供此 API，无需额外状态管理。

## Risks / Trade-offs

- **硬编码颜色与主题系统脱节** → 接受，品牌色不需要主题切换；深色模式下 #EAEDF3 作为亮色标题在深色背景上对比度良好
- **"All Posts" 目标页** → 当前 `BlogListScreen` 已支持完整分页列表，可直接复用；若需独立路由，后续再拆分
- **236dp 固定宽度在窄屏（< 236dp）上溢出** → 概率极低（最小支持 Android 手机宽度约 360dp），可接受；若需适配可改为 `fillMaxWidth(0.6f)`

## Open Questions

- "All Posts →" 跳转目标：是否需要新建独立路由，还是复用现有首页（去掉 Featured 分区）？当前方案：复用现有 `BlogListScreen`，通过 `Screen.Home` 路由跳转。

