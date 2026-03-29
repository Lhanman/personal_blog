## MODIFIED Requirements

### Requirement: 顶部导航栏布局
顶部导航栏 SHALL 根据当前页面层级在品牌入口与页面上下文之间切换，并在 desktop 端与窗口顶部融合为单一头部区域；在顶层页面显示 Logo，在层级页面显示返回入口与当前页标题。桌面端（≥768dp）导航链接与工具栏保持水平排列，移动端（<768dp）继续折叠为汉堡菜单。

#### Scenario: 桌面端顶层页面显示品牌导航
- **WHEN** 当前页面为 Home、Posts、Tags、About、Search 或 Admin 列表等顶层页面
- **THEN** 头部左侧显示 Logo 或品牌名称，不显示返回按钮，右侧显示导航链接、Search、主题切换与登录态操作

#### Scenario: 桌面端层级页面显示返回与标题
- **WHEN** 当前页面为文章详情、标签文章列表、后台编辑页等非顶层页面
- **THEN** 头部左侧显示返回按钮和当前页标题，右侧仍保留导航工具区，整个头部与窗口顶边视觉连续

#### Scenario: 移动端继续使用折叠菜单
- **WHEN** 视口宽度 < 768dp
- **THEN** 头部保留 Logo、Search、主题切换与汉堡菜单模式，不因 desktop 专属壳层变更而丢失现有移动端交互

## ADDED Requirements

### Requirement: 返回上一页交互
系统 SHALL 为存在父级关系的页面提供返回上一页入口，并优先回退真实导航历史；当不存在可回退历史时，系统 MUST 回退到该页面定义的逻辑父路由。

#### Scenario: 从文章列表进入详情后返回
- **WHEN** 用户从 Home、Posts、Search 或 TagPosts 页面进入 `Screen.PostDetail` 后点击返回按钮
- **THEN** 系统返回到实际来源页面，并保留该页面已有浏览状态

#### Scenario: 冷启动详情页的降级返回
- **WHEN** 用户直接进入 `Screen.PostDetail` 或 `Screen.AdminPostEdit` 且当前导航栈无可回退历史时点击返回按钮
- **THEN** 系统导航到该页面配置的逻辑父页面，并避免重复创建相同顶层目的地

#### Scenario: 顶层页面不显示返回入口
- **WHEN** 当前页面被标记为顶层页面
- **THEN** 头部不显示返回按钮，也不会占用品牌入口区域
