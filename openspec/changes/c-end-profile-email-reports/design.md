## Context

C 端「我的」二级只有身体资料 / 账号安全 / 退出登录；退出无确认。顶栏右侧是静态「Train Log」。登录仅用户名+密码；`work_out_user` 无邮箱。分享报告已有创建与公开读取，仓储已有 `findByUserIdOrderByCreatedAtDesc`，但没有 C 端列表 API。产品文档曾把邮箱验证列为不做，本次明确纳入绑定与邮箱验证码登录。

约束：身份只信 JWT；禁止 N+1；java-architecture-master（class/method JavaDoc、调用点注释、变更 public 方法 `log.info` 脱敏）；TDD 先红后绿。

## Goals / Non-Goals

**Goals:**

- 「我的」增加报告记录三级页，列出本人分享并打开 `/report/{id}`。
- 顶栏右上角：已登录显示用户名；未登录显示「登录」。
- 绑定 / 解绑邮箱；4 位数字验证码；绑定后可用邮箱+验证码登录。
- 账号相关操作二次确认弹窗（退出、改密、绑定、解绑、注销）。

**Non-Goals:**

- OAuth / 微信 / 短信 / 找回密码 / 改用户名。
- 用邮箱注册新账号（须先用户名密码注册，再绑定邮箱）。
- 分页、删除分享、改分享内容。
- 把 CMS 报告能力搬进 C 端编辑。

## Decisions

### D1: 邮箱挂在已有用户上，不另开身份体系

**Choice:** `work_out_user.email` 可空、唯一（MySQL 允许多个 NULL）。绑定后该邮箱只能对应一个账号。用户名密码登录始终可用。邮箱登录只签发与现网相同 JWT（subject=username, claim uid）。

**Alternatives:** 邮箱即用户名（破坏现有 3–32 字母数字规则）；独立账号表（过度设计）。

### D2: 验证码 4 位数字，用途三分，落库哈希

**Choice:** 用途 `BIND` / `UNBIND` / `LOGIN`。`POST /api/v1/auth/email/sendCode`：`email` + `purpose`。码 1000–9999（避免前导零歧义，仍是 4 位），TTL 10 分钟，同一 email+purpose 60 秒内不可重发。表 `work_out_email_code` 存 `code_hash`（BCrypt 或 SHA-256+pepper），禁止明文。校验成功即标记 used。未绑定邮箱不能发 `LOGIN`/`UNBIND`；`BIND` 要求 JWT 且该邮箱未被他人占用。

公开接口：`sendCode`、`loginByEmail`。需 JWT：`bind`、`unbind`、`GET /me`。

**Alternatives:** 6 位（用户指定 4 位）；把码返回给前端（仅测试 profile 用 CapturingEmailSender，不进 API）。

### D3: 发信抽象，未配 SMTP 时打日志便于本地

**Choice:** `EmailSender` 接口。配置了 `spring.mail.host` 则 SMTP；否则 `LoggingEmailSender` 打 INFO（含验证码，方便本地/演示）。测试 profile 用内存捕获 sender，用例从 bean 取 lastCode。不把码放进 HTTP 响应。

### D4: C 端报告列表复用仓储，不走 admin API

**Choice:** `GET /api/v1/shareReports`（JWT）返回 `{ list: [{ id, from, to, createdAt }] }`，`id` 为公开 token。一次 `findByUserIdOrderByCreatedAtDesc`。USER 看不到他人报告。前端 `/profile/reports`。SPA fallback 已覆盖 `/profile/**`。

### D5: 顶栏账号来自会话，登录/注册写入 username

**Choice:** `AuthContext` 增加 `username`，localStorage `workout_username`。`setSession(token, role, username)`。挂载时若有 token 则 `GET /api/v1/auth/me` 刷新 username/email/role。顶栏：有 username 显示账号（链到 `/profile`）；未登录显示「登录」→ `/login?redirect=<当前 path>`。替换「Train Log」。

### D6: 二次确认为应用内 dialog，不用再依赖原生 confirm 作为唯一入口

**Choice:** 轻量 `ConfirmDialog`（role=dialog）：标题+说明+取消/确认。覆盖：退出登录、修改密码提交前、绑定邮箱提交前、解绑、注销。取消不发请求。测试按 dialog 文案点确认/取消。

### D7: TDD

后端：`EmailBindLoginTest`、`MyShareReportsListTest`、`AuthMeTest`。前端：扩展 `ProfilePage.test.tsx`、`AppShell.test.tsx`、`loginFormValidation.test.tsx`。

## Risks / Trade-offs

- [4 位码易被猜] → 60 秒重发、10 分钟过期、失败次数上限（5 次作废）、不在 API 回显。
- [无 SMTP 时代码进日志] → 仅 Logging 实现；生产配 SMTP 后日志只打脱敏邮箱。
- [邮箱唯一索引与历史 NULL] → MySQL 多 NULL 合法；绑定前查占用。

## Migration Plan

Flyway `V8__user_email.sql`：`work_out_user.email`；`work_out_email_code` 表。回滚：停用新接口与前端入口，保留列无害。

## Open Questions

无。未配 SMTP 时以日志验证码完成绑定/登录（演示环境可接受）。
