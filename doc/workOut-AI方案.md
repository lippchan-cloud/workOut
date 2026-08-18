# workOut AI 方案（DeepSeek）

| 项 | 内容 |
| --- | --- |
| 产品 | workOut |
| 文档类型 | AI 调用方案 |
| 版本 | v1.0 |
| 日期 | 2026-08-18 |

## 模型与端点

- 提供商：DeepSeek
- 模型：`deepseek-chat`（便宜聊天模型；无 Google 式 flash 名）
- Base URL：`https://api.deepseek.com`
- 路径：`POST /chat/completions`（OpenAI 兼容）
- 鉴权：`Authorization: Bearer <用户绑定的 apiKey>`

## 鉴权与密钥

- 表 `work_out_api_key`：**密钥库**（独立管理明文 Key）；CMS `GET/POST /api/v1/admin/apiKeys/pool` 可看/新增（响应仅掩码）。
- 表 `work_out_user_api_key`：一用户一行绑定，含 `pool_id`；**新注册**时从密钥库取绑定最少的一把默认关联（库空则跳过）。
- 配置项 `workout.ai.deepseek.api-key` ← 环境变量 `WORKOUT_DEEPSEEK_API_KEY`（私有仓可有默认值）。
- 启动种子 `DeepSeekApiKeySeeder`：把配置 key 写入密钥库，并赋给 `workout.ai.deepseek.seed-usernames`（默认 `demo,lipp`）。
- CMS：`PUT /api/v1/admin/apiKeys/{userId}`、`PUT /api/v1/admin/apiKeys/batch`；响应与列表仅 **keyMask**。
- **日志禁止打印完整 key**，只打掩码（如 `****fd29`）。

## 限流（MySQL 权威）

- 对象：**每个 apiKey（token）**，不是每个用户。
- 额度：每小时 ≤10，每天 ≤100（时区 `Asia/Shanghai`）。
- 实现：调用前对 `work_out_ai_call_log` 做 SQL `COUNT(*)`（按 `api_key_id` + 窗口起点）；通过后再调模型并插入日志。
- **禁止** Redis、Caffeine/Guava 等内存计数器作为唯一闸门。

## 调用时机

1. 用户已绑定 key 时，`POST /api/v1/shareReports` 落库 `adviceStatus=PENDING`，HTTP 立即返回。
2. 事务提交后 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` 触发 `ShareAdviceService`。
3. 无 key → `NONE_KEY` + 「未配置 API Key」，不调模型。
4. 超限 / 模型失败 → `FAILED` + 中文文案；**分享创建本身不 500**。

## 失败降级

| 情况 | 行为 |
| --- | --- |
| 无 API Key | `NONE_KEY`，展示「未配置 API Key」 |
| 限流 | `FAILED`，展示频繁提示；不调 DeepSeek |
| HTTP/超时/空回复 | `FAILED`，展示「建议生成失败…」 |
| 测试 | `workout.ai.deepseek.stub=true` 使用 `StubDeepSeekClient`，禁止打外网 |

## 相关代码

- `HttpDeepSeekClient` / `StubDeepSeekClient`
- `ShareAdviceService`、`AiRateLimitService`
- Cursor Skill：`.cursor/skills/physio-scientist-advice/SKILL.md`
