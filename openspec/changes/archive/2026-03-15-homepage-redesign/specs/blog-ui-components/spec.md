## MODIFIED Requirements

### Requirement: HeroSection 组件
系统 SHALL 提供 `HeroSection` 可复用组件，展示博客名称、个人介绍文字，使用指定的视觉规格。

#### Scenario: 展示博客基本信息
- **WHEN** `HeroSection` 被渲染
- **THEN** 显示博客名称 "LhanBoyy"（字号 48sp，颜色 #EAEDF3，字间距 0.03em）和个人介绍文字（"我是 LhanBoyy，该博客用 Kotlin Multiplatform 编写，用于对技术和生活的小记录"）

#### Scenario: 深色主题适配
- **WHEN** 系统处于深色主题
- **THEN** 博客名称颜色保持 #EAEDF3，介绍文字使用次要文字色

### Requirement: SectionTitle 组件
系统 SHALL 提供 `SectionTitle` 组件，统一各页面分区标题的样式，分割线使用品牌色。

#### Scenario: 渲染分区标题
- **WHEN** `SectionTitle` 被渲染并传入标题文字
- **THEN** 显示加粗的分区标题，下方有宽 236dp、高 2dp、颜色 #AB4B08 的品牌色分割线
