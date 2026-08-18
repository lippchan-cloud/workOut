## Why

一期 CMS 只有一张账户列表，运营无法从后台进入用户详情或预览已分享的报告。现在要把 `/cms` 做成带功能栏的后台：账户列表、用户详情、报告列表，并保留一个概览占位，仍仅 ADMIN JWT 可进。

## What Changes

- CMS 独立于普通三 Tab：侧栏或顶栏功能栏，当前栏高亮。栏位：**概览**、**账户列表**、**用户详情**、**报告**。
- 账户列表点用户名进入 `/cms/users/:userId`，展示用户名、角色、创建时间、昵称、身高、体重、最近记录摘要，以及该用户已有分享链接（链到公开 `/report/:id`）。
- 功能栏「报告」进入 `/cms/reports`，列出管理员可见的分享报告（token/id、userId、用户名、from、to、createdAt），可新窗口或站内打开公开报告。
- 新增 `GET /api/v1/admin/accounts/{userId}` 与 `GET /api/v1/admin/reports`（ADMIN only；无 token 401；USER 403）。禁止 N+1；身份只信 JWT。
- **不**用管理员身份代用户伪造/生成报告内容；CMS 只读已有分享快照。
- 同步产品/功能文档 CMS 章节与必要时技术架构；TDD 留证；不 commit。

## Capabilities

### New Capabilities

- `admin-cms`: CMS 功能栏、概览、账户列表、用户详情、分享报告列表；ADMIN JWT 鉴权与深链。

### Modified Capabilities

- `ui-hierarchy`: CMS 仍独立于底部三 Tab；深链 `/cms/**` 与 `/cms` 一样回退 SPA。
- `share-report`: 管理员只读列出已有分享；公开报告仍按 token 打开，CMS 不代用户生成快照。

## Impact

- 前端：`CmsLayout` 功能栏；路由 `/cms`、`/cms/accounts`、`/cms/users/:userId`、`/cms/reports`；Vitest（功能栏、详情、未登录跳登录）。
- 后端：`modules.admin` 扩展详情与报告列表；`ShareReportRepository` 批量列出 + 用户名 IN 查询；`SpaFallbackController` 增加 `/cms/**`。
- 文档：产品/功能 CMS 章节、技术架构 API 表、TDD 验证记录、main specs（sync）。
