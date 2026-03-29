## 1. 数据库与索引准备

- [ ] 1.1 在 `backend/src/main/resources/db/migration/` 新增中文检索优化 migration，启用 `pg_trgm` 并创建搜索相关索引
- [ ] 1.2 调整 `backend/src/test/kotlin/com/personalblog/backend/TestDb.kt`，为测试环境补齐新 migration 所需的兼容初始化策略

## 2. 后端搜索实现

- [ ] 2.1 重构 `backend/src/main/kotlin/com/personalblog/backend/repository/PostRepository.kt`，实现查询归一化、分层检索与相关性排序
- [ ] 2.2 调整 `backend/src/main/kotlin/com/personalblog/backend/routes/PostRoutes.kt`，补齐搜索参数校验并保持降级路径的 API 行为一致

## 3. 验证与文档

- [ ] 3.1 扩展 `backend/src/test/kotlin/com/personalblog/backend/repository/PostRepositoryTest.kt`，覆盖中文召回、排序优先级与降级匹配
- [ ] 3.2 扩展 `backend/src/test/kotlin/com/personalblog/backend/routes/ApiRoutesTest.kt`，覆盖搜索接口的空结果与兼容返回行为
- [ ] 3.3 更新 `README.md`，说明中文搜索策略、PostgreSQL 扩展依赖与部署注意事项

## 4. 编译与回归验证

- [ ] 4.1 运行 `./gradlew :backend:test` 验证 Repository 与 API 测试
- [ ] 4.2 运行 `./gradlew test` 做全量回归检查
