## ADDED Requirements

### Requirement: Desktop 沉浸式窗口头部
desktop 应用 SHALL 提供统一的窗口头部表面，将 titlebar 与应用 TopBar 视觉融合，并让内容区域从该头部下方连续展开，不出现明显的双层顶部条带。

#### Scenario: Desktop 启动后渲染统一头部
- **WHEN** 用户启动 desktop 应用
- **THEN** 窗口顶部显示单一头部表面，页面内容与头部之间仅保留设计定义的分隔，而不是系统 titlebar 与应用 TopBar 的双重分离效果

#### Scenario: 主题切换时头部视觉保持一致
- **WHEN** 用户在 desktop 端切换浅色或深色主题
- **THEN** 头部背景、标题文字、分隔线和交互图标同步使用对应主题语义色，保持沉浸式视觉连续性

### Requirement: Desktop 头部拖拽与操作区分离
Desktop 头部 SHALL 在不影响导航交互的前提下提供窗口拖拽能力，且交互控件区域 MUST 不被拖拽热区覆盖。

#### Scenario: 通过头部空白区域拖动窗口
- **WHEN** 用户在 desktop 头部的非交互空白区域按下并拖动
- **THEN** 窗口随拖拽移动

#### Scenario: 点击交互控件时不触发窗口拖拽
- **WHEN** 用户点击返回按钮、导航项、Search、主题切换或窗口操作按钮
- **THEN** 系统只执行对应交互，不触发窗口拖拽
