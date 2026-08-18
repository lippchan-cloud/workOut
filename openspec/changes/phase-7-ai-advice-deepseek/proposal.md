## Why

六期公开报告已预留「建议分析」空态。运营与用户需要在分享后异步调用 DeepSeek，按成长曲线、时段事项与身高体重生成减脂/健康参考建议；同时必须按 apiKey 限流、按 userId 隔离上下文，并在 CMS 管理 key 与调用情况。

## What Changes

- 新增用户–apiKey 关联表；CMS 支持单用户改 key、批量用户改 key（掩码展示，日志禁止完整 key）。
- 每个 apiKey（token）每天最多 100 次、每小时最多 10 次调用；计数用 SQL 聚合，写入 `work_out_ai_call_log`。
- CMS 新增功能栏：**API Key**、**AI 调用**；可按 userId、apiKey（id/掩码）筛选调用情况。
- 分享创建 HTTP 不阻塞：落库后异步触发「智能生理科学家」skill 流程，调用 DeepSeek（优先 `deepseek-chat`）填报告 `advice`；报告页可显示「生成中」/失败文案/「未配置 API Key」。
- 问 AI 时带压缩上下文 + 用户信息 + **userId**（禁止串用户）；本地用 MySQL 做简易向量化/摘要压缩（不上独立向量库）。
- 交付：仓库内 Cursor Skill、`doc/workOut-AI方案.md`、`doc/workOut-上下文工程.md`；同步产品/功能/技术架构中「建议分析」从空态改为 AI 生成。
- **不**做真实医疗诊断、SSO、多环境拆分；**不** commit。

## Capabilities

### New Capabilities

- `ai-advice`: DeepSeek 调用、apiKey 绑定、按 key 限流、分享异步建议分析、上下文压缩与 userId 隔离、调用日志。

### Modified Capabilities

- `share-report`: `advice` 从空占位改为异步 AI 状态机（PENDING / READY / FAILED / NO_KEY）；公开 GET 可返回状态与文案。
- `admin-cms`: 功能栏增加 API Key 管理与 AI 调用情况；ADMIN only。
- `ui-hierarchy`: CMS 深链增加 `/cms/api-keys`、`/cms/ai-calls`；报告页建议分析展示生成中/内容/失败。

## Impact

- 后端：新表与模块（apiKey、ai call log、context compress、DeepSeek Client 可替换 bean）；Share 创建后 `@Async`/事件触发；Admin API。
- 前端：CMS 两栏页；ReportPage 按 `adviceStatus`/`advice` 渲染。
- 文档与 Skill：AI 方案、上下文工程、产品/功能/架构同步；`.cursor/skills/` 生理科学家 skill。
- 测试：Mock DeepSeek（不打外网）；窄测限流、userId 边界、CMS 鉴权、异步状态。
