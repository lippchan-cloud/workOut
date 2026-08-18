## Context

workOut 已是 React SPA + Spring Boot 单体：JWT 保护 `/api/v1/**`（`/api/v1/auth/**` 与 `/api/v1/health` 除外），用户表 `work_out_user`（含 `password_hash`），资料表 `work_out_profile`（昵称/身高/体重）。前端三 Tab 壳对未登录点击会跳登录。现在需要独立后台 CMS 列出全部账户；第一阶段明确**无需密码**即可访问。

约束：沿用现有模块分层（`modules.{api,application,infrastructure}`）、统一信封 `{ code, msg, data }`、TDD 红绿留证、java-architecture-master（public 方法 `log.info`、禁止 for-loop 查库）、无鉴权不得扩散到普通用户 API。

## Goals / Non-Goals

**Goals:**
- 独立 URL `/cms` 打开账户列表页，未登录可进
- `GET /api/v1/admin/accounts` 无 Token 返回 200 与账户列表
- 列表字段：userId、username、createdAt、nickname、heightCm、weightKg（无资料则为 null）
- JSON 与 UI 均不含 `passwordHash` / 密码
- 业务 API 仍 401（无 Token）
- 页面与代码注释标明临时开放

**Non-Goals:**
- 管理员登录、角色、审计日志
- 改密、删除用户、编辑资料
- 分页/筛选/搜索（账户量按 MVP 全量列出）
- 新数据库表或 Flyway 迁移

## Decisions

### D1: 入口用 `/cms` + 登录页链接，而不是第四个 Tab

**Choice:** SPA 路由 `/cms` 与 `/login` 同级（不包 AppShell 三 Tab）；登录页底部显眼链接「后台管理（临时开放）」。`SpaFallbackController` 增加 `/cms`。

**Alternatives:** `/admin`（同样可用，但「CMS」与产品导航更易区分）；第四 Tab（会与未登录守卫冲突，且把运营入口混进用户产品）。

**Rationale:** 需求要求独立、显眼、不把无鉴权扩散进用户壳。

### D2: 只放行一条只读 API

**Choice:** `GET /api/v1/admin/accounts` `permitAll`。`SecurityConfig` matcher 写精确路径，注释写明 TEMPORARY。其它 `/api/v1/**` 仍 `authenticated()`。

**Alternatives:** 整个 `/api/v1/admin/**` 放行（后续若加写接口会误开放）；独立端口/独立应用（过重）。

### D3: 新模块 `modules.admin`，复用 User/Profile 仓储

**Choice:**
```
com.workout.modules.admin
  api        AdminAccountController, AdminAccountResponse
  application AdminAccountService
  infrastructure（无新实体；用 UserRepository + ProfileRepository）
```
查询：`userRepository.findAll(Sort.by createdAt DESC, id DESC)` 一次；`profileRepository.findByUserIdIn(userIds)` 一次；内存按 userId 组装。空用户列表则跳过资料查询。

**Alternatives:** JOIN 自定义查询（更快但引入新 Repository 方法与投影）；循环 `findByUserId`（禁止 N+1）。

### D4: 响应契约

`data.list[]` 每项：
- `userId` Long
- `username` String
- `createdAt` Instant
- `nickname` String | null
- `heightCm` BigDecimal | null
- `weightKg` BigDecimal | null

DTO 不包含 password 字段，Jackson 因此无法序列化哈希。

### D5: 前端独立页 + 临时横幅

`CmsPage` 用 `apiGet` 拉列表；未登录不跳转（接口公开）。页顶 `role="status"` 文案：「临时开放：第一阶段无需登录即可访问，后续将加鉴权」。空资料显示「—」。不渲染任何 hash。

### D6: TDD

1. 后端 `AdminAccountsListTest`：无 Token 200、含已注册用户与资料、JSON 无 `passwordHash`；`JwtAuthFilterTest` 回归业务 API 仍 401
2. `SpaHostingTest`：`GET /cms` forward index.html
3. 前端 `CmsPage.test.tsx`：未登录可见横幅与表格字段；登录页有 CMS 入口
4. 每步红绿写入 `doc/workOut-TDD验证记录.md` 后再勾选 tasks

## Risks / Trade-offs

- [Risk] 第一阶段无鉴权等于任何人都能枚举全部账户（含昵称/身高/体重） → Mitigation：仅私有部署；页面与 Security 注释标明临时；后续必须加鉴权；不返回密码哈希
- [Risk] 全量 `findAll` 在测试库积累大量用户后变慢 → Mitigation：MVP 接受；后续分页走 DB `Page`，禁止内存切片
- [Risk] SPA `apiGet` 对 401 会清 token 跳登录 → Mitigation：本接口公开不返回 401；勿把 CMS 请求打到业务 API

## Migration Plan

无需 DDL。部署即生效。回滚：删除 permitAll 规则与 `/cms` 路由即可恢复「仅 JWT」。

## Open Questions

无。路径固定为 `/cms` 与 `GET /api/v1/admin/accounts`。
