## 1. 路由语义与头部状态建模

- [x] 1.1 在 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/navigation/Screen.kt` 或邻近导航文件中补充页面标题、顶层标记与逻辑父路由 metadata
- [x] 1.2 在 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/navigation/AppNavHost.kt` 中基于当前 back stack 导出统一的 header state（是否显示返回、标题文本、返回目标）

## 2. Desktop 壳层与沉浸式头部

- [x] 2.1 在 `frontend/composeApp/src/desktopMain/kotlin/com/personalblog/app/` 新增 desktop 窗口壳层组件，承载窗口拖拽区与沉浸式顶部容器
- [x] 2.2 调整 `frontend/composeApp/src/desktopMain/kotlin/com/personalblog/app/main.kt`，将 desktop 入口接入新的窗口壳层并保留现有窗口尺寸状态
- [x] 2.3 更新 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/PersonalBlog.kt`，让 common UI 接收 desktop shell 注入的 header/surface 能力而不破坏其他平台入口

## 3. 顶部导航与返回交互重构

- [x] 3.1 重构 `frontend/composeApp/src/commonMain/kotlin/com/personalblog/app/ui/components/Header.kt`，支持顶层页面显示品牌、层级页面显示返回按钮与标题
- [x] 3.2 为 desktop 头部实现导航工具区、拖拽空白区与窗口操作区分离，避免点击交互时触发窗口拖拽
- [x] 3.3 接通返回按钮行为：优先 `popBackStack()`，无历史时回退到逻辑父路由，并校验文章详情、标签文章与后台编辑页场景

## 4. 主题与视觉一致性验证

- [x] 4.1 调整头部背景、分隔线、标题与图标颜色，确保浅色/深色主题下 titlebar/topbar 视觉连续
- [ ] 4.2 手动验证 desktop 顶层页面与层级页面的头部样式、返回入口显隐和窗口拖拽体验

## 5. 编译与回归验证

- [x] 5.1 运行 `./gradlew :composeApp:run` 验证 desktop 端可启动且交互正常
- [x] 5.2 运行 `./gradlew :composeApp:test` 验证前端现有测试未回归
