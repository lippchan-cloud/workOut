## Context

`/cms` 现为独立 SPA（不包 `AppShell` 三 Tab），仅 ADMIN JWT 可进，页面只有 `GET /api/v1/admin/accounts` 账户表。分享报告已有：用户 `POST /api/v1/shareReports` 生成 token，公开 `GET /api/v1/reports/{token}` 与 `/report/:id`。CMS 没有功能栏、没有用户详情、没有运营侧报告列表。

约束：身份只信 JWT；禁止 N+1；java-architecture-master（public `log.info`、JavaDoc）；TDD 先红后绿；增量叠在二～六期未 commit 改动上，不还原、不 commit。

## Goals / Non-Goals

**Goals:**

- CMS 功能栏：概览、账户列表、用户详情、报告；当前栏高亮。
- 账户列表点用户名 → `/cms/users/:userId`；展示账户+资料+最近记录摘要+该用户已有分享链接。
- `/cms/reports` 列出全站已有分享；打开公开 `/report/:id`。
- `GET /api/v1/admin/accounts/{userId}`、`GET /api/v1/admin/reports` 仅 ADMIN；USER 403；无 token 401。
- 深链 `/cms/**` 回退 SPA；未登录进 CMS 任意子路径仍跳 `/login?redirect=...`。

**Non-Goals:**

- 管理员代用户 `POST /api/v1/shareReports` 或伪造快照（不偷偷用 ADMIN 身份生成用户报告）。
- 分页/筛选/编辑账户/改角色/看密码。
- 把 CMS 塞进底部三 Tab。
- 组织 IAM、SSO。

## Decisions

### D1: 顶栏功能栏 + 嵌套路由，而不是第四个 Tab

**Choice:** `CmsLayout` 包一层顶栏 tabs（贴合现有 `cms-page` 宽屏表格，不必挤左侧）。路由：

| 栏 | 路径 |
| --- | --- |
| 概览 | `/cms` |
| 账户列表 | `/cms/accounts`（`/cms` 不再只渲染表；原列表迁到此路径，也可从概览链过去） |
| 用户详情 | `/cms/users` 空态提示「请从账户列表选择用户」；`/cms/users/:userId` 详情 |
| 报告 | `/cms/reports` |

未登录访问上述任一路径 → `/login?redirect=` 当前 pathname（`/cms` 仍为 `/login?redirect=/cms`）。

**Alternatives:** 左侧栏（宽屏更好但移动端 CMS 用得少，顶栏改动更小）；默认 `/cms` 仍是账户表（功能栏会少一个「概览」落点）。

### D2: 详情与报告列表两个只读 API；分享只读已有链接

**Choice:**

- `GET /api/v1/admin/accounts/{userId}`：一次 `findById` 用户 + 一次 `findByUserId` 资料 + 一次 count + 一次 `findTop5ByUserIdAndDeletedFalseOrderByRecordedAtDescIdDesc` 最近记录 + 一次 `findByUserIdOrderByCreatedAtDesc` 该用户分享。单用户路径，每类数据各一次查询，禁止循环查库。
- `GET /api/v1/admin/reports`：一次 `findAll` 分享（按 createdAt 倒序）+ `findAllById`/`findByIdIn` 批量用户名。禁止 for-loop `findById`。
- 详情与报告列表的分享项字段：`id`（公开 token）、`userId`、`username`、`from`、`to`、`createdAt`。点开 `target=_blank` 到 `/report/{id}`。

**CMS 不代用户分享：** 不新增 `POST /api/v1/admin/shareReports`。运营若用户尚未分享，详情只显示空态「该用户暂无分享报告」。

**Alternatives:** 管理员 impersonate 生成最近一周报告（产品明确禁止伪造内容）；详情里再嵌一个生成按钮调用户 API（会用 ADMIN JWT 当该用户，错误）。

### D3: 概览不新开 API

**Choice:** 概览页复用 `GET /api/v1/admin/accounts` 与 `GET /api/v1/admin/reports` 展示账户数、分享数与两个入口链接。避免第三个几乎无信息的 endpoint。

### D4: 鉴权复用现有 ADMIN 校验

**Choice:** 抽出 `AdminAccountService.requireAdmin(operatorUserId)`（或私有方法）给 list/detail/reports 共用；角色以 DB `UserEntity.role` 为准，不信 JWT claim 或客户端 userId。未知 `userId` 详情 404。

### D5: SPA fallback `/cms/**`

**Choice:** `SpaFallbackController` 在 `/cms` 外增加 `/cms/**`，覆盖 `/cms/accounts`、`/cms/users/1`、`/cms/reports`。现有 `/cms` 测试保留；新增一条深链测。

### D6: TDD

后端：`AdminUserDetailTest`、`AdminReportsListTest`（可同文件或分文件）。前端：扩展 `CmsPage.test.tsx`（功能栏、详情、`/cms/reports`、未登录 `/cms/users/1` 与 `/cms/reports` 跳登录）。按 `doc/workOut-TDD规范.md` 先红留证再绿。

## Risks / Trade-offs

- [全表扫描分享/账户] → 当前用户量演示级；不做分页。若变慢再加 limit。
- [token 出现在 CMS 列表] → 与公开 URL 同一标识，运营需要它才能打开报告；JSON 仍不含 password。
- [账户列表 URL 从 `/cms` 挪到 `/cms/accounts`] → 书签会落到概览；概览提供「账户列表」入口。可接受。

## Migration Plan

无 DDL。部署即生效。回滚：去掉新路由与两个 GET。

## Open Questions

无（代分享已定为不做）。
