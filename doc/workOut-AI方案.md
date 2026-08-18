# workOut AI 方案（DeepSeek）

| 项 | 内容 |
| --- | --- |
| 产品 | workOut |
| 文档类型 | AI 调用方案 |
| 版本 | v1.1 |
| 日期 | 2026-08-18 |
| 配套 | [上下文工程](./workOut-上下文工程.md)、Skill `.cursor/skills/physio-scientist-advice/SKILL.md` |

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
5. 问模型时：本次压缩摘要 + 同用户历史压缩询问（总长上限 1000 字，当前优先）；系统提示强制 **简体中文 Markdown**。公开报告「建议分析」按 Markdown 渲染。

## 上下文压缩与向量化

本期要解决的不是「全站语义搜索」，而是：每次分享建议时，把**该用户**的训练/饮食快照压成短文本，去重落库，再按预算拼进 DeepSeek，且绝不串用户。

### 目标

| 目标 | 说明 |
| --- | --- |
| 控 token | 事项 content 截断、条数上限；拼 prompt 总长 ≤ **1000** 字（当前优先） |
| 可追溯 | 每块摘要带 `userId=`，并落到 `work_out_ai_context_chunk` |
| 去重 | 同用户相同摘要不重复插入 |
| 隔离 | 一切读写带 `userId`；禁止跨用户复用 chunk / 向量 |
| 可演进 | 落库时写出简易向量，日后可改为同用户内相似检索，而不必先上独立向量库 |

### 选型评估

约束：现网是 **SQLPub MySQL 8**；单体 Spring Boot；单用户历史分享量小；限流与业务数据已在 MySQL；不上 Redis、不上独立检索集群。

| 方案 | 语义质量 | 运维 / 成本 | 与现有栈 | 用户隔离 | 本期结论 |
| --- | --- | --- | --- | --- | --- |
| **Milvus / Qdrant / Weaviate** | 高（配合专用 embedding 模型） | 新进程、备份、网络、权限模型 | 与现有单体 + SQLPub 不匹配 | 要自建 collection 级隔离 | **不做**。运维面大于收益 |
| **PostgreSQL + pgvector** | 中高 | 要换主库或双写 | 主库是 MySQL，迁移成本高 | 行级 `user_id` 可做 | **不做**。不为向量换库 |
| **Redis Stack 向量** | 中 | 新组件；重启丢数据风险 | 限流已明确禁止 Redis 作权威 | 要额外 key 设计 | **不做**。与「MySQL 权威」原则冲突 |
| **MySQL 原生 VECTOR / HeatWave** | 依赖版本 | SQLPub 为 MySQL 8.0，无可靠 VECTOR 索引 | 看似同栈，实际不可用 | — | **不做**。环境不具备 |
| **云 embedding API**（OpenAI / DeepSeek embedding 等） | 高 | 每块摘要多一次计费与限流；密钥与延迟 | 可调，但分享链路已占 DeepSeek chat 额度 | 仍要自管存储 | **本期不做**。会叠耗 key 额度；可作升级选项 |
| **本地 hashing-trick 向量 + SHA-256 去重，全部落现有 MySQL** | 低（词袋哈希，非语义模型） | 零新组件；随业务表备份 | 完全复用 SQLPub | `user_id` 条件查询，天然隔离 | **选定** |

**选定理由（一句话）**：在「单用户、短上下文、必须隔离、不能加基础设施」的前提下，MySQL 内存摘要 + 精确 hash 去重已经够用；32 维 hashing-trick 只作为同库可演进的向量痕迹，而不是对外 ANN 引擎。

**明确不选独立向量库的原因**：

1. 每个用户可进入 prompt 的历史本身被 1000 字截断，召回集极小，ANN 没有规模优势。
2. 错误召回（把 A 的饮食块拼进 B）比「召回不够准」危害更大，隔离必须在 SQL 层用 `user_id` 做，而不是只靠向量距离。
3. 分享建议是异步写报告，不是在线搜索框，不需要毫秒级百万级检索。

### 选定方案（落地）

表 `work_out_ai_context_chunk` 同时承担 **压缩文档库** 和 **简易向量库**：

| 列 | 角色 | 算法 |
| --- | --- | --- |
| `summary_text` | 喂给模型的压缩询问 | 快照字段拼接：`userId`、displayName、区间、身高体重、事项（最多 40 条，content 最多 40 字）、consume/intake 计数 |
| `embed_hash` | 精确去重 | 规范化摘要的 **SHA-256 hex**；查询 `user_id + embed_hash`，仅同用户命中才复用 |
| `embedding_json` | 简易向量 | **hashing trick / bag-of-hash**：分词后 `floorMod(token.hashCode(), 32)` 累加，得到长度 **32** 的 float 数组，序列化为 JSON 文本存 MySQL `MEDIUMTEXT` |
| `source_type` / `source_ref` | 溯源 | 如 `SHARE` + share token |
| `user_id` | 隔离边界 | 所有查找必须带此列 |

实现类：`AiContextCompressService`（`compressAndStore`、`toEmbeddingJson`、`assembleWithHistory`）。

```
分享快照
  → 压缩为 summary_text（强制首行 userId=）
  → SHA-256 → embed_hash（同用户命中则复用行，不重复插入）
  → hashing-trick 32 维 → embedding_json
  → INSERT work_out_ai_context_chunk
  → assembleWithHistory：本次摘要优先，再按 created_at 新→旧拼同用户最近 ≤20 条
      总长超过 1000 字则停止追加更早历史（当前本身超限不截断）
  → PhysioScientistPrompts.userMessage → DeepSeek
```

**两套「像向量、实际不同」的机制不要混用：**

| 机制 | 现在是否参与拼 prompt | 解决什么 |
| --- | --- | --- |
| `embed_hash` 精确匹配 | 写入路径去重 | 同一用户重复分享同一段摘要不灌库 |
| 按时间取最近历史 + 1000 字预算 | **是，当前检索策略** | 把历史压缩询问带给模型，控 token |
| `embedding_json` 余弦 / 点积 | **否**（已落库，检索未启用） | 预留：同用户内「相似历史」替换纯时间序 |

hashing-trick **不是** 语义 embedding：近义词、语序变化不会稳定靠近。它的价值是零依赖、可复现、和摘要一起备份；语义检索要升级时，可替换 `toEmbeddingJson`（例如改为调用 embedding API），表结构可不变。

### 升级门槛（何时才值得换方案）

同时满足再评估，而不是先上 Milvus：

1. 单用户 chunk 经常远超 20 条，且「最近」不再等于「相关」（例如跨月对比减脂）。
2. 准备引入专用 embedding 模型，并接受额外额度。
3. 仍坚持 **先按 userId 过滤再算距离**，禁止全局向量搜索。

那时优先顺序：同表对 `embedding_json` 做应用层余弦（候选仍 ≤20）→ 仍不够再考虑 pgvector / 独立库。

## 失败降级

| 情况 | 行为 |
| --- | --- |
| 无 API Key | `NONE_KEY`，展示「未配置 API Key」 |
| 限流 | `FAILED`，展示频繁提示；不调 DeepSeek |
| HTTP/超时/空回复 | `FAILED`，展示「建议生成失败…」 |
| 测试 | `workout.ai.deepseek.stub=true` 使用 `StubDeepSeekClient`，禁止打外网 |

## 相关代码

- `HttpDeepSeekClient` / `StubDeepSeekClient`
- `ShareAdviceService`、`AiRateLimitService`、`AiContextCompressService`
- Cursor Skill：`.cursor/skills/physio-scientist-advice/SKILL.md`
