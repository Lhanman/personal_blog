## Why

当前博客内容以中文为主，但数据库迁移仍使用 `english` `tsvector`，运行时主路径也退化为 `ILIKE`。这会导致中文召回差、排序粗糙，并在文章量增长后放大全表扫描与响应波动风险，因此需要系统性优化检索方案。

## What Changes

- 调整搜索能力，使中文关键词检索具备更稳定的召回、排序与降级行为
- 为 PostgreSQL 增加适合中文内容的索引与查询策略，并保留无扩展场景的兼容路径
- 补充搜索性能与结果质量验证，明确后续可观测与调优入口

## Capabilities

### New Capabilities
- `search-performance`: 约束搜索接口在常见查询下的响应退化策略与验证方式

### Modified Capabilities
- `blog-search`: 将搜索行为从简单包含匹配升级为面向中文内容的分层检索、排序与降级匹配

## Impact

影响 `backend` 搜索 Repository / Routes、Flyway migration、PostgreSQL 扩展与索引配置，可能涉及 `shared` 搜索响应字段；前端搜索页仅需兼容更稳定的排序与空状态，不改平台架构。
