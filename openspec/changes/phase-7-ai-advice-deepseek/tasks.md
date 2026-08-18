## 1. Schema & domain

- [ ] 1.1 Flyway `V6__ai_advice.sql`：`work_out_user_api_key`、`work_out_ai_call_log`、可选 `work_out_ai_context_chunk`；share 表加 `advice_status`
- [ ] 1.2 Entity / Repository：UserApiKey、AiCallLog、ContextChunk；ShareReport 支持 adviceStatus 读写

## 2. DeepSeek client & rate limit (TDD)

- [ ] 2.1 **TDD** `DeepSeekClient` 接口 + test stub；禁止测试打外网
- [ ] 2.2 **TDD** 按 `apiKeyId` SQL 聚合：小时 10 / 天 100；超限不调模型
- [ ] 2.3 **TDD** 无 apiKey → `NONE_KEY` + 「未配置 API Key」，不调模型

## 3. Context compress & async advice (TDD)

- [ ] 3.1 上下文压缩服务：事项/曲线/身高体重摘要 + userId；同 userId hash 可选落 MySQL
- [ ] 3.2 **TDD** 分享提交后异步 Job：stub 成功 → READY；stub 失败 → FAILED；create HTTP 不 500
- [ ] 3.3 **TDD** 公开 GET 返回 `advice` + `adviceStatus`；userId 边界（禁止串用户上下文）

## 4. CMS API keys & call logs (TDD)

- [ ] 4.1 **TDD** `GET/PUT /api/v1/admin/apiKeys`、`PUT .../batch`：ADMIN 200、USER 403、匿名 401；响应仅 mask
- [ ] 4.2 **TDD** `GET /api/v1/admin/aiCalls?userId=&apiKeyId=`：筛选 + 无完整 key + 无 N+1

## 5. Frontend CMS & report

- [ ] 5.1 CMS 功能栏加 API Key、AI 调用；页面 `/cms/api-keys`、`/cms/ai-calls`；SPA fallback
- [ ] 5.2 **TDD** ReportPage：PENDING「生成中」、NONE_KEY「未配置 API Key」、READY 文案、FAILED 文案
- [ ] 5.3 Vitest：CMS 新栏导航与未登录 redirect

## 6. Skill & docs

- [ ] 6.1 交付 `.cursor/skills/physio-scientist-advice/SKILL.md`（智能生理科学家）
- [ ] 6.2 写 `doc/workOut-AI方案.md`、`doc/workOut-上下文工程.md`
- [ ] 6.3 同步产品/功能/技术架构「建议分析」为 AI 生成；TDD 验证记录留证
- [ ] 6.4 openspec-sync-specs 将 delta 合入 main specs
