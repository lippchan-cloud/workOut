# workOut 本地上下文工程

| 项 | 内容 |
| --- | --- |
| 产品 | workOut |
| 文档类型 | 上下文工程架构 |
| 版本 | v1.0 |
| 日期 | 2026-08-18 |

## 目标

分享异步建议时，向 DeepSeek 提供**压缩后的上下文** + 用户信息 + **userId**，在保证可追溯的同时控制 token，并严格按用户隔离。

## 原则

1. **不上独立向量库**（Milvus / PGVector / 外部检索服务均不做）。
2. **不上 Redis** 存向量或限流状态。
3. 压缩摘要、hash、简易 embedding **全部落 MySQL**。
4. 一切查询必须带 **userId**，禁止跨用户复用 chunk。

## 表：`work_out_ai_context_chunk`

| 列 | 含义 |
| --- | --- |
| user_id | 隔离边界 |
| source_type / source_ref | 如 SHARE + share token |
| summary_text | 压缩后的文本上下文（喂给模型） |
| embed_hash | 摘要 SHA-256，同用户去重 |
| embedding_json | 简易 hashing-trick 向量（JSON 数组，存 TEXT） |

## 压缩流水线

1. 从分享快照取：显示名、区间、事项（截断 content、限制条数）、曲线首尾身高体重。
2. 文本首行强制 `userId={id}`。
3. 计算 `embed_hash`；`findFirstByUserIdAndEmbedHash` 命中则复用（**仅同 userId**）。
4. 未命中：生成 `embedding_json`（固定维度 bag-of-hash），与摘要一并 `INSERT`。
5. 将 `summary_text` 拼入 `PhysioScientistPrompts.userMessage(userId, compressed)`。

## 分享异步流水线

```
POST /shareReports
  → 事务：写 share（PENDING 或 NONE_KEY）
  → AFTER_COMMIT 事件
      → SQL 限流（ai_call_log COUNT）
      → compressAndStore（MySQL）
      → DeepSeek chat
      → 更新 snapshot.advice + advice_status
      → INSERT ai_call_log（SUCCESS/FAILED/RATE_LIMITED）
```

公开 `GET /reports/{id}` 返回 `advice` + `adviceStatus`；报告页 PENDING 时可短轮询「生成中」。

## userId 边界

- 事件携带 `userId`，必须与 `ShareReport.userId` 一致，否则拒绝。
- 压缩服务拒绝空 userId；prompt 必须含 `userId=`。
- 一个 apiKey 服务多用户时，靠 **userId** 做上下文边界，禁止把 A 用户事项拼进 B 的请求。
