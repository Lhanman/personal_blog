## Context

当前实现存在两层错配：数据库迁移在 `backend/src/main/resources/db/migration/V1__initial_schema.sql` 中使用 `to_tsvector('english', ...)`，而 `backend/src/main/kotlin/com/personalblog/backend/repository/PostRepository.kt` 的在线查询主路径仍是 `ILIKE`。对于以中文为主的文章，这会带来三类问题：中文词边界无法被 `english` parser 正确处理、结果缺少相关性排序、数据量增长后容易退化为高成本扫描。

整体数据链路建议如下：

```text
搜索页输入
  -> SearchViewModel 300ms debounce
    -> GET /api/v1/posts/search?q=...
      -> PostRepository 归一化查询
        -> PostgreSQL 分层检索
           |- 优先：全文/分词检索（可用扩展时）
           |- 其次：trigram 相似度召回
           `- 最后：ILIKE 降级兜底
      -> 按统一 score 排序后返回
```

## Goals / Non-Goals

**Goals:**
- 提升中文文章搜索的召回率与排序稳定性
- 避免搜索主路径长期依赖全字段 `ILIKE`
- 在 PostgreSQL 扩展不可用时保留兼容降级能力
- 为后续用 `EXPLAIN ANALYZE`、慢查询日志做调优留出可观测入口

**Non-Goals:**
- 不引入 Elasticsearch、Meilisearch 等外部搜索引擎
- 不改动前端 MVVM、导航或主题体系
- 不在本次变更中实现复杂推荐、拼音检索或向量语义搜索

## Decisions

| 决策 | 理由 | 替代方案 |
| --- | --- | --- |
| 采用“查询归一化 + 分层检索 + 统一排序”策略 | 单一方案难同时兼顾中文召回、模糊匹配与可回退性；分层策略更适合当前体量 | 仅保留 `ILIKE`：实现简单，但性能和排序最差 |
| PostgreSQL 优先使用 `pg_trgm` + GIN/GiST 索引，保留中文分词扩展接入点 | `pg_trgm` 官方扩展易部署，可显著改善中文短词与 typo/子串搜索；后续可再接 `pg_jieba/zhparser` | 直接依赖中文分词扩展：效果更强，但 Docker 与托管环境兼容性更高风险 |
| 搜索排序由“发布时间优先”改为“相关性优先，发布时间兜底” | 用户更关心命中度；同分时再看新旧更符合搜索预期 | 继续按时间排序：实现简单，但优质结果常被埋没 |
| 保留 API 兼容，优先不改 `shared` DTO | 当前前端只依赖文章列表，无需为 score 暴露额外字段即可完成第一阶段优化 | 新增 `score/snippet` 字段：可读性更好，但会扩大跨模块变更面 |

## Risks / Trade-offs

- [`pg_trgm` 仍可能对超短词效果有限] → 通过最小查询长度、归一化、标题加权与 `ILIKE` 兜底缓解
- [多策略 SQL 更复杂] → 将查询构建收敛到 Repository 内部，并补充 Repository 测试与 SQL explain 验证
- [不同部署环境扩展可用性不一致] → migration 设计为可检测、可降级；Docker 镜像明确启用扩展
- [排序变化可能影响旧用例断言] → 调整测试为验证“高相关结果靠前”，避免过度绑定固定顺序

## Migration Plan

1. 新增 Flyway migration，启用 `pg_trgm`，补充归一化表达式索引与必要的 GIN/GiST 索引。
2. 在 `PostRepository` 引入查询归一化与分层检索 SQL，默认走相关性排序。
3. 补充 Repository 测试、接口回归测试，并用 `EXPLAIN ANALYZE` 校验查询计划。
4. 更新 Docker / 部署说明，确保本地与部署环境能启用扩展；若失败则回退至兼容路径。
5. 发布后观察慢查询日志与搜索命中反馈；若中文分词仍不足，再评估引入 `pg_jieba/zhparser`。

## Open Questions

- 生产 PostgreSQL 是否允许安装额外扩展，还是只能使用内置 `pg_trgm`？
- 是否需要第二阶段暴露 `score` 或 `snippet` 给前端做高亮与解释？
