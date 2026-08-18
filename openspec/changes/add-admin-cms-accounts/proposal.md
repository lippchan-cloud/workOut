## Why

运营/调试需要在后台一眼看到全部注册账户（用户名、创建时间、资料可见字段），但当前产品只有按 JWT 隔离的「我的」资料接口。第一阶段明确要求管理页无需密码即可访问，以便尽快核对账号；必须把无鉴权范围收在独立 CMS 入口，避免污染普通用户业务 API。

## What Changes

- 新增独立 CMS 管理页（路由 `/cms`），未登录即可打开；页面显著标明「临时开放、后续将加鉴权」
- 新增公开只读接口 `GET /api/v1/admin/accounts`，列出全部账户的用户名、创建时间与资料可见字段
- **禁止**在接口或页面返回/展示 `passwordHash` 或明文密码
- Security 仅对该 CMS 列表接口（及 `/cms` SPA 深链）放行；`/api/v1/dailyRecords/**`、`/api/v1/profile` 等业务 API 仍须 Bearer JWT
- 登录页提供显眼「后台管理」入口，直达 `/cms`
- 无新表、无分页（第一阶段列出全部账户）；资料用批量查询拼接，禁止 N+1

## Capabilities

### New Capabilities

- `admin-cms`: 临时无鉴权的后台账户列表（独立 `/cms` 页 + 只读 accounts API；不泄露密码哈希；不放行其它业务 API）

### Modified Capabilities

- （无 — 不修改既有 user-auth / daily-record / user-profile 的 SHALL；CMS 是额外的窄放行例外）

## Impact

- 后端：新模块 `modules.admin`（Controller/Service/DTO）；`UserRepository`/`ProfileRepository` 批量查询；`SecurityConfig` 增加 `GET /api/v1/admin/accounts` permitAll；`SpaFallbackController` 增加 `/cms`
- 前端：独立 `CmsPage`（不走三 Tab 壳守卫）、登录页 CMS 入口、Vitest 覆盖未登录可进与字段展示
- 安全：第一阶段无密码是**临时**措施，仅限本仓库内网/私有部署调试；后续必须加鉴权
- 测试：按 `doc/workOut-TDD规范.md` 先红后绿，证据写入 `doc/workOut-TDD验证记录.md`
