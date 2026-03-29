## 1. 创建 Header 组件文件

- [x] 1.1 在 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/` 创建 `Header.kt`，定义 `AppHeader` Composable 函数签名（参数与现有 `AppTopBar` 一致）
- [x] 1.2 实现 `NavItem` 组件：使用 `Box` 包裹文字，激活时在文字下方叠加 2dp 橙色（`#FF6B01`）高亮线，去除 `TextDecoration.Underline`
- [x] 1.3 实现 Search 图标按钮：使用 `Icons.Default.Search` 图标，激活时图标颜色为 accent 色

## 2. 实现响应式布局

- [x] 2.1 在 `AppHeader` 中使用 `BoxWithConstraints` 检测宽度，以 768dp 为断点区分桌面/移动端
- [x] 2.2 桌面端（≥768dp）：Logo 左对齐，导航链接 + 工具栏右对齐，水平排列
- [x] 2.3 移动端（<768dp）：Logo 左对齐，汉堡菜单图标（`Icons.Default.Menu` / `Icons.Default.Close`）右对齐
- [x] 2.4 移动端展开菜单：用 `var menuExpanded by remember { mutableStateOf(false) }` 控制，展开时在顶部栏下方垂直列出导航项
- [x] 2.5 导航项点击后自动关闭移动端菜单（`menuExpanded = false`）

## 3. 修复 Logo 颜色

- [x] 3.1 将 Logo 文字颜色从硬编码 `Color(0xFFEAEDF3)` 改为 `MaterialTheme.colorScheme.onBackground`

## 4. 集成到 AppNavHost

- [x] 4.1 在 `AppNavHost.kt` 中将 `AppTopBar(...)` 调用替换为 `AppHeader(...)`，删除原 `AppTopBar` 和 `NavItem` 私有函数
- [x] 4.2 确认 `HorizontalDivider` 分隔线保留（或改用 `BrandDivider`）

## 5. 验证

- [x] 5.1 运行 `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` 确认 Web 端编译通过，桌面端导航正常显示
- [x] 5.2 手动验证：桌面端导航激活状态高亮线正确，Logo 颜色随主题切换
- [x] 5.3 手动验证：移动端（缩小浏览器窗口至 <768dp）汉堡菜单展开/收起正常
- [x] 5.4 运行 `./gradlew :composeApp:test` 确认现有测试无回归
