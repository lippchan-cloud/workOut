## Context

六期已落地分享快照与公开报告页，「建议分析」仍为 `advice: null` 空态。CMS（含未 commit 的功能栏：概览/账户/用户详情/报告）已具备 ADMIN JWT 与深链。本变更在其上增量：DeepSeek 异步填建议、用户–apiKey 绑定、按 key 限流、CMS 管理与调用观测、本地 MySQL 上下文压缩。

约束：Asia/Shanghai；数据按 JWT `userId` 隔离；日志禁止完整 API Key；测试不打真实外网；分享 POST 不得因 AI 失败而 500。

## Goals / Non-Goals

**Goals:**

- 用户绑定 DeepSeek apiKey 后，分享落库即异步生成「建议分析」；无 key 显示「未配置 API Key」。
- 每个 apiKey：每天 ≤100、每小时 ≤10（SQL 聚合计数）。
- CMS：单/批量改 key；调用情况按 userId / apiKey 筛选。
- 压缩上下文 + userId 注入模型，禁止串用户。
- 交付 Skill + AI 方案文档 + 上下文工程文档，并同步产品/功能/架构文档。

**Non-Goals:**

- 真实医疗诊断、独立向量库、SSO、多环境拆分、commit、全量 mvn test 打满 SQLPub。

## Decisions

### 1. 模型与 HTTP 客户端

- **Choice:** DeepSeek OpenAI 兼容 Chat Completions；模型 `deepseek-chat`；`baseUrl=https://api.deepseek.com`；Authorization `Bearer <userApiKey>`。
- **Why:** 用户指定 DeepSeek 与最便宜聊天模型；官方无 Google 式 flash 名。
- **Alt:** 全局环境变量 key — 拒绝，需用户–key 关联与限流。
- **Impl:** `DeepSeekClient` 接口 + `HttpDeepSeekClient` / `StubDeepSeekClient`（测试 `@Primary` 或 `@Profile("test")`）。

### 2. 表结构

```sql
-- work_out_user_api_key: id, user_id UNIQUE, api_key (私有仓可明文), key_mask, updated_at, updated_by
-- work_out_ai_call_log: id, user_id, api_key_id, purpose (SHARE_ADVICE), status, share_token NULL, created_at
-- work_out_ai_context_chunk (可选): id, user_id, source_type, source_ref, summary_text, embed_hash, created_at
```

分享表增量：`advice_status`（NONE_KEY / PENDING / READY / FAILED）、`advice_text`（或继续写进 snapshotJson 的 advice 字段 + 列存 status）。

**Choice:** `advice_status` 列 + 更新 `snapshot_json.advice`，公开 API 同时返回 `advice` 与 `adviceStatus`。

### 3. 限流

- **对象:** `api_key_id`（不是 user）。
- **实现:** 调用前  
  `COUNT(*) WHERE api_key_id=? AND created_at >= hour_start` / `>= day_start`（上海时区边界）。  
  超限：写 FAILED 日志（可选）、报告 `adviceStatus=FAILED` 文案「调用过于频繁」，分享 HTTP 仍 200。
- **禁止:** 拉全量日志到内存再数。

### 4. 异步流水线

1. `POST /api/v1/shares` 事务内：建快照（advice=null, status=PENDING 或 NONE_KEY）、返回 token/url。
2. 事务提交后 `ApplicationEvent` / `@Async`：`ShareAdviceJob`。
3. Job：查用户 key → 无则 `NONE_KEY`；有则限流 → 压缩上下文（仅该 userId）→ 读 skill 系统提示 → DeepSeek → 写 advice + READY；异常 FAILED，可 CMS/手动重试接口可选（最小：公开页展示失败，再次分享可重试）。
4. **禁止**在 create 事务内同步 HTTP 调模型。

### 5. 上下文工程（MySQL）

- 输入：范围事项摘要、曲线首尾/斜率简述、身高体重、displayName、**userId**。
- 压缩：截断过长 content；事项按日聚合计数；可选 `embed_hash = SHA-256(规范化摘要)` 去重存 `work_out_ai_context_chunk`。
- 不上向量检索库；相似复用仅同 userId 的 hash 命中。
- Prompt 显式：`userId=...`；Client 层断言请求上下文 userId == 分享行 userId。

### 6. CMS API

| Method | Path | 说明 |
|--------|------|------|
| GET/PUT | `/api/v1/admin/apiKeys` / `{userId}` | 列表/单用户改；body `request.apiKey` |
| PUT | `/api/v1/admin/apiKeys/batch` | `request.userIds` + `request.apiKey` |
| GET | `/api/v1/admin/aiCalls?userId=&apiKeyId=` | 调用情况，掩码展示 |

ADMIN JWT；USER 403；匿名 401。前端栏：`/cms/api-keys`、`/cms/ai-calls`。

### 7. Skill 与文档

- Skill：`.cursor/skills/physio-scientist-advice/SKILL.md`（系统角色、输入字段、输出约束「仅供参考」、禁止诊断口吻）。
- `doc/workOut-AI方案.md`、`doc/workOut-上下文工程.md`。
- 运行时 Job 内嵌与 skill 对齐的常量系统提示（或 classpath 资源），保证无 Cursor 时也能跑。

### 8. TDD

- RED→GREEN：限流 SQL、无 key、CMS 403、Mock Client 成功写 advice、userId 隔离（构造错误上下文应拒绝）、报告页状态文案。
- 窄测类名如 `AiAdviceRateLimitTest`、`AdminApiKeyTest`、`ShareAdviceAsyncTest`。

## Risks / Trade-offs

- [异步丢任务 / 进程重启] → Mitigation：PENDING 可定时扫（最小可不做；文档写明可扩展）。
- [Key 明文入库] → 私有仓可接受；CMS 掩码；日志只打 mask / id。
- [模型胡言 / 医疗风险] → Skill + 文案「仅供参考」；非诊断。
- [限流竞态] → 可接受近似；必要时后续 UNIQUE 窗口表。

## Migration Plan

1. Flyway `V6__ai_advice.sql` 建表 + share 列。
2. 部署后旧分享 `advice_status` 默认视为空建议（READY 空或 NULL 显示占位）。
3. 回滚：停 Job、忽略新列；不删历史 log。

## Open Questions

- 无：模型名固定 `deepseek-chat`；限流按 key；异步不阻塞 POST。
