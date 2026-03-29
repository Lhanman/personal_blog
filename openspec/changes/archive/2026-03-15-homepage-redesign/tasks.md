## 1. 原子组件

- [x] 1.1 新建 `BrandDivider.kt`：宽 236dp、高 2dp、颜色 `Color(0xFFAB4B08)`，水平居中
- [x] 1.2 新建 `Footer.kt`：展示 "Copyright © 2024 | All rights reserved."，文字居中，使用次要文字色

## 2. 更新现有组件

- [x] 2.1 更新 `HeroSection.kt`：博客名称字号 48sp、颜色 `Color(0xFFEAEDF3)`、letterSpacing 0.03em；个人介绍文字固定为 "我是 LhanBoyy，该博客用 Kotlin Multiplatform 编写，用于对技术和生活的小记录"
- [x] 2.2 更新 `SectionTitle.kt`：将 `HorizontalDivider` 替换为 `BrandDivider` 组件

## 3. 重写 TopBar

- [x] 3.1 重写 `AppNavHost.kt` 中的 `AppTopBar`：左侧博客名称 Logo（TextButton，点击跳转首页），右侧导航链接（Home、About、Tags、Search）+ 主题切换按钮 + 登录/登出按钮
- [x] 3.2 在 `AppTopBar` 中通过 `navController.currentBackStackEntryAsState()` 获取当前路由，对当前页面对应的导航链接添加高亮样式（下划线或颜色区分）

## 4. 重写首页布局

- [x] 4.1 更新 `BlogListScreen.kt`：Featured 分区固定取最多 3 篇（带 "featured" 标签），Recent Posts 分区固定取最新 3 篇（非 featured）
- [x] 4.2 在 `BlogListScreen.kt` 的 Recent Posts 分区底部添加居中 "All Posts →" TextButton，触发 `onAllPostsClick` 回调
- [x] 4.3 更新 `AppNavHost.kt` 中 `BlogListScreen` 的调用，传入 `onAllPostsClick` 回调（导航到 `Screen.Home` 路由）
- [x] 4.4 在 `BlogListScreen.kt` 底部添加 `BrandDivider` + `Footer` 组件，各分区之间使用 `BrandDivider` 分隔

## 5. 验证

- [x] 5.1 运行 `./gradlew :composeApp:wasmJsBrowserDistribution` 确认编译无报错
- [x] 5.2 运行 `./gradlew :composeApp:test` 确认测试通过
