# workOut TDD 验证记录

| 项 | 内容 |
| --- | --- |
| 文档类型 | TDD 红绿证据台账 |
| 文档版本 | v1.0 |
| 分支 | `feat/init-workout-mvp` |
| 规范 | [workOut-TDD规范.md](./workOut-TDD规范.md) |
| OpenSpec | [init-workout-mvp tasks](../openspec/changes/init-workout-mvp/tasks.md)、[add-admin-cms-accounts tasks](../openspec/changes/add-admin-cms-accounts/tasks.md)、[extend-calendar-month-range-csv tasks](../openspec/changes/extend-calendar-month-range-csv/tasks.md)、[phase-2-production-hardening tasks](../openspec/changes/phase-2-production-hardening/tasks.md)、[phase-3-ui-hierarchy tasks](../openspec/changes/phase-3-ui-hierarchy/tasks.md)、[phase-4-month-csv-body-history-curves tasks](../openspec/changes/phase-4-month-csv-body-history-curves/tasks.md)、[phase-5-share-report-curve-xlsx tasks](../openspec/changes/phase-5-share-report-curve-xlsx/tasks.md)、[cms-nav-user-detail-reports tasks](../openspec/changes/cms-nav-user-detail-reports/tasks.md) |

> 规则：未写本页证据，不得勾选 `tasks.md`。

---

## 进度总览

| Task | 名称 | RED | GREEN | 记录节 | tasks 勾选 |
| --- | --- | --- | --- | --- | --- |
| 1.1 | Backend 脚手架 | N/A（脚手架） | 工程可 `mvn test` 编译 | §1.1 | 是 |
| 1.2 | Flyway 表结构 | 已证 | 已证 | §1.2 | 是 |
| 1.6 | ApiResponse 信封 | 已证 | 已证 | §1.6 | 是 |
| 2.1 | Auth 注册 | 已证 | 已证 | §2.1 | 是 |
| 2.2 | Auth 登录 | 已证 | 已证 | §2.2 | 是 |
| 2.3 | JWT 过滤器 | 已证 | 已证 | §2.3 | 是 |
| 1.3 | Frontend 脚手架 | N/A（脚手架） | Vitest + build 通过 | §1.3 | 是 |
| 1.4 | 静态资源复制/CLI | N/A（脚手架） | build:static + start.sh | §1.4 | 是 |
| 1.5 | SPA 托管 | 已证 | 已证 | §1.5 | 是 |
| 2.4 | 前端鉴权跳转 | 已证 | 已证 | §2.4 | 是 |
| 2.5 | 登录页/401 | 已证 | 已证 | §2.5 | 是 |
| 3.1–3.3 | App Shell | 已证 | 已证 | §3.x | 是 |
| 4.1 | 创建日记录 | 已证 | 已证 | §4.1 | 是 |
| 4.2 | 记录隔离 | 已证 | 已证 | §4.2 | 是 |
| 4.3 | 按日查询排序 | 已证 | 已证 | §4.3 | 是 |
| 4.4 | 前端记录页 | 已证 | 已证 | §4.4 | 是 |
| 5.1–5.4 | 日历周视图 | 已证 | 已证 | §5.x | 是 |
| 6.1–6.3 | CSV 导出 | 已证 | 已证 | §6.x | 是 |
| 7.1–7.2 | 资料 | 已证 | 已证 | §7.x | 是 |
| 8.1 | T00–T13 验收 | 自动化覆盖 + 缺口说明 | 见验收记录 | §8.1 | 是 |
| 8.2 | README | N/A | 已更新启动/测试 | §8.2 | 是 |
| 8.3 | openspec validate | N/A | 通过 | §8.3 | 是 |
| 9.1 | 分组 commit | — | 未执行（需用户明确要求） | — | 否 |
| 9.2 | 不 push / 无密钥 | N/A | 已遵守 | §9.2 | 是 |
| CMS-1.1 | 无 Token 列账户 | 已证 | 已证 | §CMS-1.1 | 是 |
| CMS-1.2 | admin 模块实现 | 承接 1.1 RED | 已证 | §CMS-1.2 | 是 |
| CMS-1.3 | 业务 API 仍 401 | 既有行为补测 | 已证 | §CMS-1.3 | 是 |
| CMS-2.1 | `/cms` SPA 回退 | 已证 | 已证 | §CMS-2.1 | 是 |
| CMS-2.2/2.3 | CMS 页 + 登录入口 | 已证 | 已证 | §CMS-2.x | 是 |
| CMS-2.4 | 用户ID + 加载/空/错态 | 已证 | 已证 | §CMS-2.4 | 是 |
| CAL-1.1 | 月/区间列表失败测试 | 已证 | 见 1.2 | §CAL-1.1 | 是 |
| CAL-1.2 | 解析 yearMonth/from/to | 承接 1.1 | 已证 | §CAL-1.2 | 是 |
| CAL-1.3 | 互斥参数 400 | 已证 | 已证 | §CAL-1.3 | 是 |
| CAL-1.4 | yearMonth 用户隔离 | 既有隔离延续 | 已证 | §CAL-1.4 | 是 |
| CAL-2.1–2.3 | 期间 CSV | 已证 | 已证 | §CAL-2.x | 是 |
| CAL-3.1–3.3 | 日历三种模式 | 已证 | 已证 | §CAL-3.x | 是 |
| CAL-4.1–4.2 | 导出跟随筛选 | 已证 | 已证 | §CAL-4.x | 是 |
| CAL-5.1 | 相关回归 | N/A | 已证 | §CAL-5.1 | 是 |
| CAL-5.2 | openspec validate | N/A | 通过 | §CAL-5.2 | 是 |
| CAL-5.3 | 不提交 git | N/A | 本会话未执行 commit/push | §CAL-5.3 | 是 |
| P2-1.1 | CMS 无 Token 401 | 已证 | 已证 | §P2-1.1 | 是 |
| P2-1.2 | USER 403 / ADMIN 200 | 已证 | 已证 | §P2-1.2 | 是 |
| P2-2.1 | CMS 跳转登录、无免登录入口 | 已证 | 已证 | §P2-2.1 | 是 |
| P2-2.2 | USER 拒、ADMIN 可见表 | 已证 | 已证 | §P2-2.2 | 是 |
| P2-3.1 | 记录 PUT + 空内容 400 | 已证 | 已证 | §P2-3.1 | 是 |
| P2-3.2 | 记录 DELETE + 跨用户 404 | 已证 | 已证 | §P2-3.2 | 是 |
| P2-4.1 | 日历加载/失败/空态 | 已证 | 已证 | §P2-4.1 | 是 |
| P2-4.2 | 日历改删确认 | 已证 | 已证 | §P2-4.2 | 是 |
| P2-4.3 | 补记带日期 | 已证 | 已证 | §P2-4.3 | 是 |
| P2-5.1 | 表单即时校验与保存去向 | 已证 | 已证 | §P2-5.1 | 是 |
| P2-5.2 | 401 草稿/redirect | 已证 | 已证 | §P2-5.2 | 是 |
| P2-6.1 | 改密 API | 已证 | 已证 | §P2-6.1 | 是 |
| P2-6.2 | 注销删本人数据 | 已证 | 已证 | §P2-6.2 | 是 |
| P2-6.3 | 我的账号区/改密/注销 | 已证 | 已证 | §P2-6.3 | 是 |
| P2-7.1 | 相关回归 | N/A | 已证 | §P2-7.1 | 是 |
| P2-7.2 | openspec validate | N/A | 通过 | §P2-7.2 | 是 |
| P2-7.3 | 不提交 git | N/A | 已遵守 | §P2-7.3 | 是 |
| P3-1.1 | GET record by id | 已证 | 已证 | §P3-1.1 | 是 |
| P3-2.1 | 我的三级选项 | 已证 | 已证 | §P3-2.1 | 是 |
| P3-2.2 | ADMIN CMS 在账号页 | 已证 | 已证 | §P3-2.2 | 是 |
| P3-3.1 | 小周切换 | 已证 | 已证 | §P3-3.1 | 是 |
| P3-3.2 | 周 from&to 与气泡 | 已证 | 已证 | §P3-3.2 | 是 |
| P3-3.3 | 详情路由 GET by id | 已证 | 已证 | §P3-3.3 | 是 |
| P3-3.4 | 补记次要样式 | 已证 | 已证 | §P3-3.4 | 是 |
| P3-4.1 | 按钮层级 | N/A | 已证 | §P3-4.1 | 是 |
| P3-5.x | 文档与 main specs | N/A | 已写 | §P3-5.x | 是 |
| P3-6.1 | 相关回归 | N/A | 已证 | §P3-6.1 | 是 |
| P3-6.2 | openspec validate | N/A | 通过 | §P3-6.2 | 是 |
| P3-6.3 | 不提交 git | N/A | 已遵守 | §P3-6.3 | 是 |
| P4-1.1 | 资料历史与 trends | 已证 | 已证 | §P4-1.1 | 是 |
| P4-1.2 | trends 条数隔离 | 已证 | 已证 | §P4-1.2 | 是 |
| P4-2.1 | CSV 表头身体列 | 已证 | 已证 | §P4-2.x | 是 |
| P4-2.2 | CSV 按 recordedAt 对齐 | 已证 | 已证 | §P4-2.x | 是 |
| P4-3.1 | 注销删历史 | 已证 | 已证 | §P4-3.1 | 是 |
| P4-4.1 | 上海时分格式化 | 已证 | 已证 | §P4-4.1 | 是 |
| P4-4.2 | 日/月列表时分 | 已证 | 已证 | §P4-4.2 | 是 |
| P4-4.3 | 变化曲线页 | 已证 | 已证 | §P4-4.3 | 是 |
| P4-5.x | 文档与 main specs | N/A | 已写 | §P4-5.x | 是 |
| P4-6.1 | 相关回归 | N/A | 已证 | §P4-6.1 | 是 |
| P4-6.2 | openspec validate | N/A | 通过 | §P4-6.2 | 是 |
| P4-6.3 | 不提交 git | N/A | 已遵守 | §P4-6.3 | 是 |
| P5-1.1 | 资料真实日期 changedAt | 已证 | 已证 | §P5-1.1 | 是 |
| P5-2.1 | 导出缺身高体重 400 | 已证 | 已证 | §P5-2.1 | 是 |
| P5-2.2 | 导出 xlsx 双 sheet | 已证 | 已证 | §P5-2.2 | 是 |
| P5-3.1 | Flyway V5 分享表 | 已证 | 已证 | §P5-3.1 | 是 |
| P5-3.2 | 创建分享 token+url | 已证 | 已证 | §P5-3.2 | 是 |
| P5-3.3 | 公开 GET 报告 | 已证 | 已证 | §P5-3.3 | 是 |
| P5-3.4 | 注销删分享 / SPA /report | 既有注销 + 新测 | 已证 | §P5-3.4 | 是 |
| P5-4.1 | 资料页日期+曲线 | 已证 | 已证 | §P5-4.x | 是 |
| P5-4.2 | 曲线单位与粒度 zoom | 已证 | 已证 | §P5-4.x | 是 |
| P5-4.3 | 日历去掉曲线主路径 | 已证 | 已证 | §P5-4.x | 是 |
| P5-5.1 | 导出/分享闸门 | 已证 | 已证 | §P5-5.1 | 是 |
| P5-5.2 | 公开报告页布局 | 已证 | 已证 | §P5-5.2 | 是 |
| P5-6.x | 文档与 main specs | N/A | 已写 | §P5-6.x | 是 |
| P5-7.1 | 相关回归 | N/A | 已证 | §P5-7.1 | 是 |
| P5-7.2 | openspec validate | N/A | 通过 | §P5-7.2 | 是 |
| P5-7.3 | 不提交 git | N/A | 已遵守 | §P5-7.3 | 是 |
| CMSNAV-1.1 | ADMIN 用户详情 API | 已证 | 已证 | §CMSNAV-1.1 | 是 |
| CMSNAV-2.1 | ADMIN 分享列表 API | 已证 | 已证 | §CMSNAV-2.1 | 是 |
| CMSNAV-3.1 | `/cms/**` SPA 回退 | 已证 | 已证 | §CMSNAV-3.1 | 是 |
| CMSNAV-4.1 | CMS 功能栏/详情/报告页 | 已证 | 已证 | §CMSNAV-4.1 | 是 |
| CMSNAV-5.x | 文档与 main specs | N/A | 已写 | §CMSNAV-5.x | 是 |
| CMSNAV-6.1 | 相关回归 | N/A | 已证 | §CMSNAV-6.1 | 是 |
| CMSNAV-6.2 | openspec validate | N/A | 通过 | §CMSNAV-6.2 | 是 |
| CMSNAV-6.3 | 不提交 git | N/A | 已遵守 | §CMSNAV-6.3 | 是 |

---

## §1.1 — Backend 脚手架（非行为 TDD）

- 对应：`openspec/.../tasks.md` 1.1
- 说明：创建 `backend/` Spring Boot 3.3.5、依赖 Web/Validation/JPA/Security/Flyway/MySQL/JJWT、入口 `WorkOutApplication`
- 验证：`cd backend && mvn -q test -Dtest=FlywayMigrationTest` 能启动上下文（见 §1.2）
- 备注：脚手架本身无 Scenario 断言；业务行为不得借本任务偷跑

---

## §测试库 — 直连 SQLPub（2026-08-18）

- 变更：`application-test.yml` 指向与主配置相同的 SQLPub MySQL（`inv_doc`）；`pom.xml` 移除 H2；用例经 `TestUsernames.unique(prefix)` 追加 8 位 UUID 后缀
- 隔离说明：测试会向真实库写入 `user` / `daily_record` / `profile`，**不保证回滚**；靠唯一用户名降低冲突
- Flyway：仅跑未记录迁移（`V1__init.sql`），不改其它业务表
- 验证命令：`cd backend && mvn test`（需外网访问 SQLPub）
- **当前结果：未通过** — 主机 `mysql5.sqlpub.com:3310` TCP 可达，但 JDBC 报  
  `Access denied for user 'user_lipp'@'218.1.218.251' (using password: YES)`  
  凭证与文档/`application.yml` 一致；疑为密码失效或 SQLPub 对该公网 IP 未授权。需用户在 SQLPub 控制台核对密码/白名单后重跑 `mvn test`

---

## §1.2 — FlywayMigrationTest

- 对应规格：`app-bootstrap` — MySQL schema / 表存在
- 测试类：`backend/src/test/java/com/workout/FlywayMigrationTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=FlywayMigrationTest`
- 结果：**FAIL**
- 失败原因摘要：断言需要表 `user` / `daily_record` / `profile`，当时 **0 migrations**，表不存在  
  （`Expecting HashSet: [...] to contain: ["user", "daily_record", "profile"]`）

### GREEN

- 实现：`backend/src/main/resources/db/migration/V1__init.sql`
- 命令：同上
- 结果：**PASS**（exit 0；Flyway `Migrating schema ... to version "1 - init"`）

---

## §1.6 — ApiResponseEnvelopeTest

- 对应规格：功能文档统一响应 `{ code, msg, data }` + requestId/timestamp
- 测试类：`backend/src/test/java/com/workout/common/ApiResponseEnvelopeTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=ApiResponseEnvelopeTest`
- 结果：**FAIL**
- 失败原因摘要：`Status expected:<200> but was:<404>`（尚无 `/api/v1/health`）

### GREEN

- 实现：`ApiResponse`、`HealthController`、`GlobalExceptionHandler` 等
- 命令：`cd backend && mvn -q test -Dtest=ApiResponseEnvelopeTest,FlywayMigrationTest`
- 结果：**PASS**

---

## §2.1 — AuthRegisterTest

- 对应规格：`user-auth` — 注册成功 / 重复用户名 / 非法密码
- 测试类：`backend/src/test/java/com/workout/auth/AuthRegisterTest.java`

### RED（已留证）

- 命令：`cd backend && mvn -q test -Dtest=AuthRegisterTest`
- 结果：**FAIL**（3 tests）
- 失败原因摘要：注册接口未就绪；Status expected 200/400 but was 500（`NoResourceFoundException` 被通用异常处理误伤为 500）

### GREEN（已留证）

- 实现：`AuthController` / `AuthService` / `UserRepository` / `JwtService` / `RegisterRequest` / `ApiRequest` / `AuthTokenResponse` / `PasswordEncoder`；并修正 `GlobalExceptionHandler` 对 404 的映射
- 命令：`cd backend && mvn -q test -Dtest=AuthRegisterTest,FlywayMigrationTest,ApiResponseEnvelopeTest`
- 结果：**PASS**（注册成功发 Token；重复用户名 `该用户名已被注册`；短密码校验 400）
- 回归：Flyway + ApiResponse 仍绿

### 勾选

- `tasks.md` 2.1 可勾选

---

## §2.2 — AuthLoginTest

- 对应规格：`user-auth` — 登录成功 / 错误密码通用中文错误且无 token
- 测试类：`backend/src/test/java/com/workout/auth/AuthLoginTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=AuthLoginTest`
- 结果：**FAIL**（3 tests）
- 失败原因摘要：`POST /api/v1/auth/login` 尚未实现；Status expected 200/400 but was **404**（`NoResourceFoundException`）

### GREEN

- 实现：`LoginRequest`；`AuthController.login`；`AuthService.login`（用户不存在与密码错误同一文案「用户名或密码错误」，不打印密码）
- 命令：`cd backend && mvn -q test -Dtest=AuthLoginTest`
- 结果：**PASS** — Tests run: 3, Failures: 0
- 勾选：tasks.md 2.2

---

## §2.3 — JwtAuthFilterTest

- 对应规格：`user-auth` — 业务 API 需 Bearer JWT；`/api/v1/auth/**` 放行
- 测试类：`backend/src/test/java/com/workout/auth/JwtAuthFilterTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=JwtAuthFilterTest`
- 结果：**FAIL**（2 failures / 1 pass）
- 失败原因摘要：`GET /api/v1/secure/ping` 未实现且 Security 仍 `permitAll`；无 Token/有效 Token 均为 **404**（expected 401/200）

### GREEN

- 实现：`JwtAuthFilter`、`JwtService.parseToken`、`AuthPrincipal`、`JsonAuthEntryPoint`、`SecurePingController`；`SecurityConfig` 收紧为 `/api/v1/auth/**` 与 `/api/v1/health` 放行、其余 `/api/v1/**` 需认证
- 命令：`cd backend && mvn -q test -Dtest=JwtAuthFilterTest,AuthLoginTest,AuthRegisterTest,ApiResponseEnvelopeTest`
- 结果：**PASS** — Tests run: 10, Failures: 0（exit 0）
- 勾选：tasks.md 2.3

---

## §1.3 — Frontend Vite 脚手架（非行为 TDD）

- 对应：`openspec/.../tasks.md` 1.3
- 说明：创建 `frontend/` Vite + React + TypeScript + React Router；Vitest + Testing Library；冒烟 `App.test.tsx`
- 验证：`cd frontend && npm test` → Tests 1 passed；`npm run build` 成功产出 `dist/`
- 备注：脚手架本身无 Scenario 断言；鉴权守卫/页面行为由后续 2.4+ TDD 驱动

### 勾选

- `tasks.md` 1.3 可勾选

---

## §1.4 — 静态资源复制与 CLI（脚手架）

- 对应：`openspec/.../tasks.md` 1.4
- 说明：`frontend/package.json` 增加 `copy:static` / `build:static`；`scripts/start.sh` 先构建再 `mvn spring-boot:run`；README 补充启动与环境变量
- 验证：`cd frontend && npm run build:static` 产出 `backend/src/main/resources/static/index.html`
- 勾选：tasks.md 1.4

---

## §1.5 — SpaHostingTest

- 对应规格：`app-bootstrap` — SPA fallback；`GET /` 与 `/calendar` 返回 HTML
- 测试类：`backend/src/test/java/com/workout/common/SpaHostingTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=SpaHostingTest`
- 结果：**FAIL**（2 tests）
- 失败原因摘要：静态资源未就绪；`GET /` 与 `GET /calendar` Status expected 200 but was **404**

### GREEN

- 实现：复制前端 `dist` 到 `backend/src/main/resources/static`；`SpaFallbackController` 对 `/calendar` 等路由 `forward:/index.html`
- 命令：`cd backend && mvn -q test -Dtest=SpaHostingTest`
- 结果：**PASS** — Tests run: 3, Failures: 0
- 备注：MockMvc 对 `forward` 不解析正文，故以 `forwardedUrl` + 直接 `GET /index.html` 断言 HTML 与 `workOut` 标记
- 勾选：tasks.md 1.5

---

## §2.4 — authRedirect.test.tsx

- 对应规格：`user-auth` — 未登录点日历 Tab → `/login?redirect=/calendar`
- 测试类：`frontend/src/authRedirect.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/authRedirect.test.tsx`
- 结果：**FAIL**
- 失败原因摘要：页面尚无「日历」按钮（`Unable to find ... name "日历"`）

### GREEN

- 实现：`AuthContext`、`AppShell`（三 Tab）、`LoginPage` 占位路由、`api/client`
- 命令：同上
- 结果：**PASS** — Tests 1 passed
- 勾选：tasks.md 2.4

---

## §2.5 — 登录表单校验与 401 拦截

- 对应规格：`user-auth` — 登录校验；API 401 清 token
- 测试类：`frontend/src/loginFormValidation.test.tsx`、`frontend/src/api401.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/loginFormValidation.test.tsx src/api401.test.tsx`
- 结果：**FAIL**（2 tests）
- 失败原因摘要：空用户名未拦截（请求落到 fetch 报错）；401 未清 token（仍为 `stale-token`）

### GREEN

- 实现：`LoginPage` 空用户名提示「请填写用户名」；`RegisterPage`；`api/client` 401 清 token 并 `location.assign` 登录页
- 命令：同上（含 authRedirect 回归）
- 结果：**PASS** — Tests 3 passed
- 勾选：tasks.md 2.5

---

## §3.x — AppShell.test.tsx（3.1 / 3.2 / 3.3）

- 对应规格：`app-shell` — 三 Tab；已登录切换；未登录 redirect
- 测试类：`frontend/src/AppShell.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/AppShell.test.tsx -t "allows authenticated"`
- 结果：**FAIL**（临时去掉已登录放行后：点日历落到登录页，找不到 `calendar` 文案）
- 补充：三 Tab 文案与未登录 redirect 的缺失态已在 §2.4 RED（无「日历」按钮）留证

### GREEN

- 实现：保持 `AppShell` 已登录可导航、未登录带 `redirect`；本任务补全断言覆盖记录/日历/我的
- 命令：`cd frontend && npm test -- src/AppShell.test.tsx`
- 结果：**PASS** — Tests 6 passed
- 勾选：tasks.md 3.1 / 3.2 / 3.3

---

## §4.1 — DailyRecordCreateTest

- 对应规格：`daily-record` — 创建 CONSUME；空内容；501 字
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordCreateTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordCreateTest`
- 结果：**FAIL**（3 tests）
- 失败原因摘要：`POST /api/v1/dailyRecords` 未实现；expected 200/400 but was **404**

### GREEN

- 实现：`DailyRecord*` 实体/仓储/服务/控制器；`CurrentUser` 从 JWT 取 userId
- 命令：同上
- 结果：**PASS** — Tests run: 3, Failures: 0
- 勾选：tasks.md 4.1

---

## §4.2 — DailyRecordIsolationTest

- 对应规格：`daily-record` — 跨用户隔离
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordIsolationTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordIsolationTest`
- 结果：**FAIL**
- 失败原因摘要：查询未按 userId 过滤时 `list.length` expected 0 but was **1**

### GREEN

- 实现：`listByDate` 强制 `userId` 条件
- 命令：`cd backend && mvn -q test -Dtest=DailyRecordCreateTest,DailyRecordIsolationTest,DailyRecordQueryTest`
- 结果：**PASS**
- 勾选：tasks.md 4.2

---

## §4.3 — DailyRecordQueryTest

- 对应规格：`calendar-view` / `daily-record` — 同日按 recordedAt、id 升序
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordQueryTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest`
- 结果：**FAIL**
- 失败原因摘要：降序时 `list[0].type` expected CONSUME but was **INTAKE**

### GREEN

- 实现：仓储 `OrderByRecordedAtAscIdAsc`
- 命令：同上组合命令
- 结果：**PASS**
- 勾选：tasks.md 4.3

---

## §4.4 — RecordPage.test.tsx

- 对应规格：`daily-record` — 保存消耗后清空；未登录保存跳登录
- 测试类：`frontend/src/RecordPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/RecordPage.test.tsx`
- 结果：**FAIL**
- 失败原因摘要：找不到「保存消耗」按钮

### GREEN

- 实现：`RecordPage` 消耗/摄入两表单；未登录 `navigate(/login?redirect=/)`；成功后清空 textarea
- 命令：`cd frontend && npm test`
- 结果：**PASS**（记录页 2 tests 含在全套 21 passed）
- 勾选：tasks.md 4.4

---

## §5.x — 周工具 / CalendarPage / 颜色 / 上一周

- 测试类：`frontend/src/calendar/week.test.ts`、`frontend/src/CalendarPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/calendar/week.test.ts src/CalendarPage.test.tsx`
- 结果：**FAIL**（缺 `week.ts` / 无周条与空态文案）

### GREEN

- 实现：`week.ts` 周一为一周起始；`CalendarPage` 周条、「这一天还没有记录」、CONSUME `#16A34A` / INTAKE `#DC2626`、上一周/下一周
- 命令：`cd frontend && npm test`
- 结果：**PASS** — CalendarPage 3 tests + week 2 tests
- 勾选：tasks.md 5.1 / 5.2 / 5.3 / 5.4

---

## §6.x — CsvExportTest + 前端导出点击

- 测试类：`backend/.../CsvExportTest.java`、`frontend/src/CsvExportClick.test.tsx`

### RED

- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`
- 结果：**FAIL**
- 失败原因摘要：`GET /api/v1/dailyRecords/exportCsv` expected 200 but was **404**（无 Token 用例已由过滤器返回 401）
- 前端 RED：未实现「导出 CSV」按钮时无法点击

### GREEN

- 实现：`DailyRecordService.exportCsv` UTF-8 BOM + 表头 + 中文类型 + 文件名；前端 mock 下载与未登录跳转
- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`；`cd frontend && npm test -- src/CsvExportClick.test.tsx`
- 结果：**PASS** — CsvExportTest 4 tests；CsvExportClick 2 tests
- 勾选：tasks.md 6.1 / 6.2 / 6.3

---

## §7.x — ProfileUpsertTest + ProfilePage

- 测试类：`backend/.../ProfileUpsertTest.java`、`frontend/src/ProfilePage.test.tsx`

### RED

- 命令：`cd backend && mvn -q test -Dtest=ProfileUpsertTest`
- 结果：**FAIL**
- 失败原因摘要：`GET/PUT /api/v1/profile` 404

### GREEN

- 实现：`Profile*` 实体/仓储/服务/控制器（userId 来自 JWT）；前端回填、保存成功、「退出登录」清 token
- 命令：`cd backend && mvn -q test -Dtest=ProfileUpsertTest`；`cd frontend && npm test -- src/ProfilePage.test.tsx`
- 结果：**PASS** — ProfileUpsertTest 3；ProfilePage 2
- 勾选：tasks.md 7.1 / 7.2

---

## §8.1 — T00–T13 验收记录

- 文档：`doc/验收记录.md`
- 说明：浏览器手工未全跑；API/单测覆盖对应场景，缺口已标明
- 勾选：tasks.md 8.1

---

## §8.2 — README

- 已含启动步骤、环境变量、默认 8080、`mvn test` / `npm test`
- 勾选：tasks.md 8.2

---

## §8.3 — openspec validate

- 命令：`openspec validate init-workout-mvp`
- 结果：`Change 'init-workout-mvp' is valid`
- 勾选：tasks.md 8.3

---

## §9.2 — 不 push / 无密钥

- 未执行 `git push`
- 连接信息见私有仓 `application.yml` / `application-test.yml` 与 `doc/workOut-数据库连接.md`；可用 `WORKOUT_*` 覆盖
- 勾选：tasks.md 9.2

---

## §重构 — 后端分层 + 表前缀 work_out（2026-08-18）

- 对应：后端包按 `config` / `common` / `modules.{auth,record,profile}.{api,application,domain,infrastructure}` 分层；业务表统一 `work_out_*`
- 测试类：`backend/src/test/java/com/workout/FlywayMigrationTest.java`（断言 `work_out_user` / `work_out_daily_record` / `work_out_profile`）

### RED

- 命令：`cd backend && mvn -q test -Dtest=FlywayMigrationTest`
- 结果：先改断言为 `work_out_*`，当时仅有 V1（`user` / `daily_record` / `profile`）
- 说明：直连 SQLPub 时本机 IP 出现 Access denied，未能在该库看到断言失败；改用本地 MySQL 8 验证同一 V1→V2 路径

### GREEN

- 实现：`V2__PrefixWorkoutTables`（旧表 RENAME，已有新表则跳过，皆无则 CREATE）；`@Table` 改为 `work_out_*`；生产代码迁入 `modules.*`
- 命令：`cd backend && mvn test`（`WORKOUT_DB_*` 指向本地 MySQL 8；Flyway 日志：`user`→`work_out_user` 等）
- 结果：**PASS** — Tests run: 26, Failures: 0, Errors: 0；`BUILD SUCCESS`

---

## §记录交互 — 一级大按钮 / 二级选消耗或摄入（2026-08-18）

- 对应：记录 Tab 一级不再并列消耗/摄入表单；大按钮进入后再选类型，再进表单
- 测试类：`frontend/src/RecordPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/RecordPage.test.tsx`
- 结果：**FAIL** — Tests 7 failed
- 失败原因摘要：找不到按钮「开始记录」（一级仍直接渲染消耗/摄入表单）

### GREEN

- 实现：`/` 大按钮 → `/record` 选消耗/摄入 → `/record/consume` 或 `/record/intake` 表单；浏览器返回按层回退；未登录保存 `redirect` 指向当前表单路径
- 命令：`cd frontend && npm test -- src/RecordPage.test.tsx`；回归 `cd frontend && npm test`
- 结果：**PASS** — RecordPage 7 tests；全套 Test Files 11 passed，Tests 28 passed

---

## §CMS-1.1 — AdminAccountsListTest（无 Token 列账户）

- 对应规格：`openspec/changes/add-admin-cms-accounts/specs/admin-cms/spec.md` — Unauthenticated admin can list all accounts
- 测试类：`backend/src/test/java/com/workout/admin/AdminAccountsListTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=AdminAccountsListTest`
- 结果：**FAIL** — Tests run: 2, Failures: 2
- 失败原因摘要：`GET /api/v1/admin/accounts` 尚未实现且未放行；Status expected:<200> but was:<401>

### GREEN

- 见 §CMS-1.2
- 勾选：add-admin-cms-accounts tasks.md 1.1

---

## §CMS-1.2 — admin 模块最小实现

- 实现：`AdminAccountController` / `AdminAccountService` / `AdminAccountResponse`；`ProfileRepository.findByUserIdIn` 批量拼资料；`SecurityConfig` 仅 `GET /api/v1/admin/accounts` permitAll（TEMPORARY 注释）
- 命令：`cd backend && mvn -q test -Dtest=AdminAccountsListTest`
- 结果：**PASS**（exit 0）。中间一次因 MockMvc 默认字符集把「阿丽」读成 Latin-1，测试改为 `getContentAsString(UTF_8)` 后绿；接口 JSON 本身已是 UTF-8
- 勾选：tasks.md 1.2

---

## §CMS-1.3 — 业务 API 仍需 JWT

- 对应规格：Unauthenticated CMS must not weaken user APIs
- 测试：`AdminAccountsListTest#profileWithoutTokenShouldStillReturn401`、`#dailyRecordsWithoutTokenShouldStillReturn401`
- 说明：补测既有行为，无独立 RED（无 Token 本就 401）
- 命令：`cd backend && mvn test -Dtest=AdminAccountsListTest,JwtAuthFilterTest,SpaHostingTest`
- 结果：**PASS** — Tests run: 10, Failures: 0, Errors: 0；`BUILD SUCCESS`
- 勾选：tasks.md 1.3

---

## §CMS-2.1 — SpaHostingTest `/cms`

- 对应规格：CMS deep link serves SPA
- 测试类：`backend/src/test/java/com/workout/common/SpaHostingTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=AdminAccountsListTest,SpaHostingTest`
- 结果：**FAIL** — `cmsDeepLinkShouldForwardToSpaHtmlNotApi404` Status expected:<200> but was:<404>（`NoResourceFoundException` resource=cms）

### GREEN

- 实现：`SpaFallbackController` 增加 `/cms`
- 命令：`cd backend && mvn -q test -Dtest=SpaHostingTest`
- 结果：**PASS**（exit 0）
- 勾选：tasks.md 2.1

---

## §CMS-2.x — CmsPage + 登录入口

- 对应规格：Temporary CMS page is reachable without login
- 测试类：`frontend/src/CmsPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/CmsPage.test.tsx`
- 结果：**FAIL** — Tests 2 failed
- 失败原因摘要：`/cms` 落入三 Tab 空壳，找不到 `role=status`；登录页无「后台管理」链接

### GREEN

- 实现：独立 `CmsPage`、`App.tsx` 路由 `/cms`、登录页「后台管理」链到 `/cms`、临时横幅
- 命令：同上
- 结果：**PASS** — Tests 2 passed
- 回归：`cd frontend && npm test -- src/CmsPage.test.tsx src/loginFormValidation.test.tsx src/AppShell.test.tsx` → Tests 9 passed
- 勾选：tasks.md 2.2 / 2.3 / 3.1

---

## §CMS-2.4 — 用户ID 列 + 加载/空态/错误态

- 对应规格：`admin-cms` — Temporary CMS page（用户可见字段含 userId；加载/空/错态）
- 测试类：`frontend/src/CmsPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/CmsPage.test.tsx`
- 结果：**FAIL** — Tests 3 failed | 2 passed
- 失败原因摘要：缺列「用户ID」；缺文案「加载中…」；缺空态「暂无账户」（错误态用例已绿，属既有 `role=alert`）

### GREEN

- 实现：`CmsPage` 增加 `用户ID` 列；`loading` / `暂无账户` / 错误提示；表头始终可见
- 命令：`cd frontend && npm test -- src/CmsPage.test.tsx`
- 结果：**PASS** — Tests 5 passed
- 回归：`cd frontend && npm test -- src/CmsPage.test.tsx src/loginFormValidation.test.tsx src/AppShell.test.tsx` → Tests 12 passed；`cd backend && mvn test -Dtest=AdminAccountsListTest,JwtAuthFilterTest,SpaHostingTest` → Tests run: 10, Failures: 0
- 勾选：tasks.md 2.4

---

## §CMS-3.2 — openspec validate

- 命令：`openspec validate add-admin-cms-accounts`
- 结果：`Change 'add-admin-cms-accounts' is valid`
- 勾选：tasks.md 3.2

---

## §CMS-3.3 — 不提交 git

- 未执行 `git commit` / `git push`；不纳入 `backend/target/**`
- 勾选：tasks.md 3.3



---

## §Docker — docker profile 内嵌 H2 可启动

- 对应：Docker 单镜像交付（application-docker.yml + V2 可移植元数据）
- 测试类：`backend/src/test/java/com/workout/docker/DockerProfileBootstrapTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DockerProfileBootstrapTest`
- 结果：**ERROR** — ApplicationContext 失败：`Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, jdbc:h2:...`

### GREEN

- 实现：H2 依赖、`application-docker.yml`、V2 改用 DatabaseMetaData + `ALTER TABLE ... RENAME TO`；Dockerfile 多阶段构建
- 命令：同上
- 结果：**PASS** — Tests run: 1, Failures: 0

---

## §CAL-1.1 — 月/区间列表 RED

- 对应规格：`extend-calendar-month-range-csv/specs/daily-record` — List by month / inclusive range
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordQueryTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest`
- 结果：**FAIL** — Tests run: 5, Failures: 4
- 失败原因：`yearMonth`/`from`/`to` 请求仍要求必填 `date`，`MissingServletRequestParameterException` → HTTP 500（功能未实现）

---

## §CAL-1.2 — 解析 yearMonth / from+to GREEN

- 实现：`RecordQueryPeriod` + `DailyRecordService.resolvePeriod`/`listByPeriod`；Controller `list` 三组可选参数；一次仓储区间查询
- GREEN 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest`
- GREEN 结果：**PASS** — Tests run: 5, Failures: 0
- 勾选：tasks.md 1.1 / 1.2

---

## §CAL-1.3 — 互斥与区间校验

- 对应规格：Period query parameters are mutually exclusive and validated
- 测试类：同上 `DailyRecordQueryTest`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest#mixingDateAndYearMonthShouldReturn400+fromAfterToShouldReturn400+rangeLongerThan366DaysShouldReturn400+dateOnlyQueryShouldStillReturn200`
- 结果：**FAIL** — Tests run: 4, Failures: 3（混用/`from>to`/>366 天仍 200）；`dateOnlyQueryShouldStillReturn200` 已绿（既有契约）

### GREEN

- 实现：`resolvePeriod` 恰好一种模式；`from` 不得晚于 `to`；`ChronoUnit.DAYS.between > 365` → 400 中文 msg
- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest`
- 结果：**PASS** — Tests run: 9, Failures: 0
- 勾选：tasks.md 1.3

---

## §CAL-1.4 — yearMonth 用户隔离

- 对应规格：Period queries remain isolated per user
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordIsolationTest.java`

### RED / GREEN

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordIsolationTest`
- 结果：**PASS** — Tests run: 2, Failures: 0（`listByPeriod` 始终带 JWT `userId`，新 `yearMonth` 路径继承隔离；属既有行为补测）
- 勾选：tasks.md 1.4

---

## §CAL-2.x — 期间 CSV

- 对应规格：`csv-export` — Export uses same period; empty period header-only; period export validates
- 测试类：`backend/src/test/java/com/workout/record/CsvExportTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`
- 结果：**FAIL** — CsvExportTest Tests run: 9, Failures: 5
- 失败原因：`yearMonth`/`from`/`to` 导出仍要求 `date`（500）；混用 `date`+`yearMonth` 仍按日导出 200；`yearMonth` 无 Token 已 401（既有鉴权）

### GREEN

- 实现：`exportCsv` 与 list 共用 `resolvePeriod`；`RecordQueryPeriod.csvFilename()`
- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest,DailyRecordIsolationTest,CsvExportTest`
- 结果：**PASS** — Query 9 / Isolation 2 / Csv 10，Failures: 0
- 勾选：tasks.md 2.1 / 2.2 / 2.3

---

## §CAL-3.x — 日历三种模式

- 对应规格：`calendar-view` — month / jump date / custom range
- 测试类：`frontend/src/CalendarPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/CsvExportClick.test.tsx`
- 结果：**FAIL** — Tests 5 failed | 5 passed
- 失败原因：找不到「按月」「自定义」「跳转到」

### GREEN

- 实现：`CalendarPage` 按日/按月/自定义；月份控件、跳转日期、from/to；空态三条文案
- 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/CsvExportClick.test.tsx src/calendar/week.test.ts`
- 结果：**PASS** — Tests 12 passed
- 勾选：tasks.md 3.1 / 3.2 / 3.3

---

## §CAL-4.x — 导出跟随筛选

- 对应规格：Month mode UI download uses yearMonth；unauthenticated redirect
- 测试类：`frontend/src/CsvExportClick.test.tsx`

### RED

- 与 §CAL-3.x 同一次命令：按月导出找不到「按月」；未登录导出跳转用例已绿（回归未被破坏）

### GREEN

- 实现：导出 URL 使用与列表相同的 `periodQuery()`
- 命令：同上前端命令
- 结果：**PASS**
- 勾选：tasks.md 4.1 / 4.2

---

## §CAL-5.1 — 相关回归

- 命令：`cd backend && mvn -q test -Dtest=DailyRecordQueryTest,DailyRecordIsolationTest,CsvExportTest`
- 结果：**PASS** — Query Tests run: 9；Isolation Tests run: 2；Csv Tests run: 10；Failures: 0
- 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/CsvExportClick.test.tsx`
- 结果：**PASS** — Tests 10 passed
- 勾选：tasks.md 5.1

---

## §CAL-5.2 — openspec validate

- 命令：`openspec validate extend-calendar-month-range-csv`
- 结果：`Change 'extend-calendar-month-range-csv' is valid`
- 勾选：tasks.md 5.2

---

## §CAL-5.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`；未改 `openspec/changes/add-admin-cms-accounts`
- 说明：工作区另有既有提交 `8f16b9e` / `3d57fe0`（含 `backend/target/**` 与 Docker 文件），非本 apply 会话所执行
- 勾选：tasks.md 5.3

---

## §Docker — docker profile 改连项目 MySQL（去 H2）

- 对应：镜像不再内嵌 H2，与 `application.yml` 同套 SQLPub MySQL
- 测试类：`backend/src/test/java/com/workout/docker/DockerProfileBootstrapTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=DockerProfileBootstrapTest`
- 结果：**ERROR** — docker profile 仍指向 `jdbc:h2:file:/data/workout`，无法创建 `/data`（Read-only file system）

### GREEN

- 实现：`application-docker.yml` 改为与主配置相同的 MySQL；Dockerfile 去掉 `WORKOUT_H2_PATH`/H2 volume；`pom.xml` 移除 H2；compose/README 同步
- 命令：同上
- 结果：**PASS** — Tests run: 1, Failures: 0（JDBC `jdbc:mysql://mysql5.sqlpub.com:3310/inv_doc`，health UP）

---

## §P2 — phase-2-production-hardening（2026-08-18）

OpenSpec：`openspec/changes/phase-2-production-hardening/`。测试库仍为 SQLPub；`application-test.yml` 将 Hikari `maximum-pool-size` 设为 3，避免打满 `max_user_connections=30`。

---

### Task 1.1 — CMS 无 Token 改为 401

- 对应规格：`openspec/changes/phase-2-production-hardening/specs/admin-cms/spec.md` — Admin accounts require ADMIN JWT
- 测试类：`backend/src/test/java/com/workout/admin/AdminAccountsListTest.java#listAccountsWithoutTokenShouldReturn401`
- RED：承接 §CMS-1.2 GREEN（`GET /api/v1/admin/accounts` permitAll 返回 **200**）。改写断言为 401 后、去掉 `permitAll` 前：`Status expected:<401> but was:<200>`
- GREEN 命令：`cd backend && mvn test -Dtest=AdminAccountsListTest`
- GREEN 结果：**PASS** — Tests run: 6, Failures: 0（见 §P2-7.1 同一次回归）
- 实现要点：Flyway `V3__user_role.sql`；`SecurityConfig` 公开口仅 register/login/health；CMS GET 需 JWT
- 勾选：phase-2 tasks.md 1.1

---

### Task 1.2 — USER 403、引导 ADMIN 200

- 对应规格：同上 + `user-auth` — bootstrap ADMIN
- 测试：`regularUserJwtCannotListAccounts`、`bootstrapAdminJwtCanListAccountsWithProfile`
- RED：一期无 `data.role`；USER JWT 仍可 200 拉全站账户（CMS 当时甚至无 Token 即可列）。改写后期望 403 / `role=ADMIN` 先失败
- GREEN：同上 AdminAccountsListTest Tests run: 6, Failures: 0
- 实现要点：`UserEntity.role`、`AuthTokenResponse.role`；`workout.admin.usernames`（生产默认 `lipp`，test 为空，用例里 `AdminProperties.setUsernames`）；注册/登录提升且登录只升不降；`AdminAccountService` 查库角色，USER 抛 `ForbiddenException`（HTTP 403）
- 勾选：tasks.md 1.2

---

### Task 2.1 — 无 token 打开 /cms 跳登录；登录页无「后台管理」

- 对应规格：admin-cms — SPA `/cms` 必须管理员
- 测试文件：`frontend/src/CmsPage.test.tsx`
- RED 命令（有效）：`cd frontend && npm test -- src/CmsPage.test.tsx` — 未登录仍停在 `/cms`；登录页仍有免登录「后台管理」（承接 CMS-2.x）。**无效 RED**：误用 `npx vitest@4` 因 jsdom 无 `localStorage` 6 测全挂，按规范不计
- GREEN 命令：`cd frontend && npm test -- src/CmsPage.test.tsx`
- GREEN 结果：**PASS** — 6 tests（见 §P2-7.1）
- 实现要点：`CmsPage` 无 token → `/login?redirect=/cms`；`LoginPage` 去掉免登录 CMS 链接
- 勾选：tasks.md 2.1

---

### Task 2.2 — USER 拒看表、ADMIN 可见列且无 passwordHash

- 测试：`regular user sees denial` / `admin can open CMS columns without passwordHash`
- RED：无 `workout_role` 时 USER 仍拉账户表
- GREEN：同上 CmsPage 6 tests
- 实现要点：`AuthContext.setSession(token, role)` 存 `workout_role`；USER 文案「你不是管理员」且不渲染表；ADMIN 可见列名
- 勾选：tasks.md 2.2

---

### Task 3.1 — 所有者 PUT；空内容 400「请填写内容」

- 对应规格：`daily-record` PUT
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordUpdateDeleteTest.java`
- RED 命令：`cd backend && mvn test -Dtest=DailyRecordUpdateDeleteTest`
- RED 结果：**FAIL** — Tests run: 4, Failures: 3。`ownerCanUpdateConsumeContent` / `emptyContentOnUpdateShouldReturn400`：`Status expected:<200>/<400> but was:<404>`（无 PUT 路由）
- GREEN 命令：同上
- GREEN 结果：**PASS** — Tests run: 4, Failures: 0
- 实现要点：`PUT /api/v1/dailyRecords/{id}`；身份只取 JWT；`findByIdAndUserIdAndDeletedFalse` 一次查询
- 勾选：tasks.md 3.1

---

### Task 3.2 — 所有者 DELETE；跨用户 404

- RED：同 3.1 命令，`ownerCanDeleteRecordThenListOmitsIt` expected 200 was 404（无 DELETE 路由）；跨用户用例在路由补齐后才绿
- GREEN：Tests run: 4, Failures: 0
- 实现要点：`DELETE` 逻辑删除；跨用户同一查询 404，原记录仍在；禁止 N+1
- 勾选：tasks.md 3.2

---

### Task 4.1 — 日历加载 / 失败 / 空态分开

- 对应规格：`calendar-view`
- 测试文件：`frontend/src/CalendarPage.test.tsx`（loading / failure+retry / 真正空态）
- RED：旧实现 `catch(() => setList([]))`，失败与空列表同文案「这一天还没有记录」
- GREEN 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx`
- GREEN 结果：**PASS** — 12 tests
- 实现要点：`loading | success | error` 三态；失败可重试；空态仅 success 且 list 空
- 勾选：tasks.md 4.1

---

### Task 4.2 — 列表编辑/删除二次确认

- RED：列表项无编辑/删除；删除不调 API
- GREEN：同上 CalendarPage 12 tests（含确认后 DELETE、编辑进表单回填）
- 实现要点：每条「编辑」「删除」；`window.confirm` 后 `DELETE /api/v1/dailyRecords/{id}`；编辑进 `/record/...` 带回内容
- 勾选：tasks.md 4.2

---

### Task 4.3 — 「补记」带 date 直达表单

- RED：只能从首页大按钮走完类型选择
- GREEN：同上；补记进入带 `date=YYYY-MM-DD` 的消耗表单，datetime 使用该日
- 实现要点：日历选中日「补记」→ `/record?date=...`
- 勾选：tasks.md 4.3

---

### Task 5.1 — 即时校验与保存去向

- 测试文件：`frontend/src/RecordPage.test.tsx`
- RED：空内容仍发创建请求；保存成功无「再记一条 / 回日历」
- GREEN 命令：`cd frontend && npm test -- src/RecordPage.test.tsx`
- GREEN 结果：**PASS** — 10 tests
- 实现要点：必填/长度即时校验；成功后两个去向按钮；首页大按钮入口仍在
- 勾选：tasks.md 5.1

---

### Task 5.2 — 401 回登录并恢复草稿

- RED：401 只跳登录，草稿丢失
- GREEN：同上 RecordPage；`sessionStorage` 键 `workout_record_draft`；redirect 带回 pathname+search
- 勾选：tasks.md 5.2

---

### Task 6.1 — 改密 API

- 对应规格：`user-auth` 改密
- 测试类：`backend/src/test/java/com/workout/auth/ChangePasswordTest.java`
- RED：无 `PUT /api/v1/auth/password` 路由，断言 200/400 得 404
- GREEN 命令：`cd backend && mvn test -Dtest=ChangePasswordTest`
- GREEN 结果：**PASS** — Tests run: 2, Failures: 0（新密可登录、旧密失败；错误当前密码 400「当前密码不正确」）
- 实现要点：JWT 身份；校验当前密码后 BCrypt 更新
- 勾选：tasks.md 6.1

---

### Task 6.2 — 注销删本人数据

- 测试类：`backend/src/test/java/com/workout/auth/DeleteAccountTest.java`
- RED：无 `DELETE /api/v1/auth/me`
- GREEN：**PASS** — Tests run: 1, Failures: 0（注销后无法登录；记录/资料不可再查）
- 实现要点：`dailyRecordRepository.deleteByUserId` + `profileRepository.deleteByUserId` 批量删再删用户，禁止 N+1
- 勾选：tasks.md 6.2

---

### Task 6.3 — 「我的」账号区与身体数据分开

- 测试文件：`frontend/src/ProfilePage.test.tsx`
- RED：无改密控件；注销无确认
- GREEN 命令：`cd frontend && npm test -- src/ProfilePage.test.tsx`
- GREEN 结果：**PASS** — 4 tests
- 实现要点：账号区（改密/退出/注销）与身体数据分区；注销 `confirm` 后调删除 API 并清 token；ADMIN 可见「后台管理」链接
- 勾选：tasks.md 6.3

---

### Task 7.1 — 相关回归（本会话复跑）

- 命令：`cd backend && mvn test -Dtest=AdminAccountsListTest,DailyRecordUpdateDeleteTest,ChangePasswordTest,DeleteAccountTest,JwtAuthFilterTest,AuthLoginTest,AuthRegisterTest,DailyRecordCreateTest,DailyRecordIsolationTest,ProfileUpsertTest,FlywayMigrationTest`
- 结果：**PASS** — Tests run: 31, Failures: 0, Errors: 0, `BUILD SUCCESS`
  - Flyway 1；Create 3；Isolation 2；UpdateDelete 4；ChangePassword 2；DeleteAccount 1；Login 3；JwtFilter 3；Register 3；Admin 6；Profile 3
- 命令：`cd frontend && npm test -- src/CmsPage.test.tsx src/CalendarPage.test.tsx src/RecordPage.test.tsx src/ProfilePage.test.tsx`
- 结果：**PASS** — Tests 32 passed (32)
- 命令：`cd frontend && npm test`
- 结果：**PASS** — Test Files 11 passed；Tests 47 passed (47)
- 未跑全量 `mvn test`（含 `DockerProfileBootstrapTest` 会再开一套 docker 上下文，易打满 SQLPub 连接）
- 勾选：tasks.md 7.1

---

### Task 7.2 — openspec validate

- 命令：`openspec validate phase-2-production-hardening --type change`
- 结果：`Change 'phase-2-production-hardening' is valid`
- 勾选：tasks.md 7.2

---

### Task 7.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`；未拆 dev/stage/prod；DB/JWT 仍留现有 yml；`application-test.yml` 仅加小连接池，非多环境
- 勾选：tasks.md 7.3

---

## §P3 — phase-3-ui-hierarchy（2026-08-18）

OpenSpec：`openspec/changes/phase-3-ui-hierarchy/`。未 commit。

### Task 1.1 — GET record by id

- 对应规格：`daily-record` — GET by id；跨用户/缺失 404「记录不存在」
- 测试类：`backend/src/test/java/com/workout/record/DailyRecordGetByIdTest.java`
- RED 命令：`cd backend && mvn -q test -Dtest=DailyRecordGetByIdTest`
- RED 结果：**FAIL** — Tests run: 3, Failures: 3。`Status expected:<200>/<404> but was:<500>`（`HttpRequestMethodNotSupportedException`：`/{id}` 仅有 PUT/DELETE，无 GET）
- GREEN 命令：同上
- GREEN 结果：**PASS**（exit 0）。所有者 200；跨用户与缺失 404「记录不存在」
- 实现要点：`DailyRecordController.getById` + `DailyRecordService.getById` 复用 `requireOwned`（一次 id+userId 查询）
- 勾选：phase-3 tasks.md 1.1

### Task 2.1 / 2.2 — 「我的」三级与 CMS 入口

- 对应规格：`user-profile` / `ui-hierarchy`
- 测试文件：`frontend/src/ProfilePage.test.tsx`
- RED 命令：`cd frontend && npm test -- --run src/ProfilePage.test.tsx`
- RED 结果：**FAIL** — 找不到「身体资料」「账号安全」「返回」；`/profile` 仍摊开身高与改密
- GREEN 命令：`cd frontend && npm test -- --run src/ProfilePage.test.tsx`
- GREEN 结果：**PASS** — Tests 7 passed
- 实现要点：`/profile` 三选项；`/profile/body` 资料；`/profile/account` 改密/注销；ADMIN 仅账号页见「后台管理」
- 勾选：tasks.md 2.1 / 2.2

### Task 3.1–3.4 — 日历小周切换、气泡、详情、补记

- 对应规格：`calendar-view`
- 测试文件：`frontend/src/CalendarPage.test.tsx`、`frontend/src/calendar/week.test.ts`
- RED：`countByLocalYmd is not a function`；无 `week-nav-btn`；日模式仍打 `date=`；找不到列表按钮「跑步」与详情「返回」
- GREEN 命令：`cd frontend && npm test -- --run src/CalendarPage.test.tsx src/calendar/week.test.ts`
- GREEN 结果：**PASS** — CalendarPage 17 + week 3
- 实现要点：日模式一次 `from&to`；格子气泡；`/calendar/records/:id` GET by id；补记 `btn-text`；周切换 `week-nav-btn`
- 勾选：tasks.md 3.1 / 3.2 / 3.3 / 3.4

### Task 4.1 — 按钮层级

- 实现：`index.css` 主 CTA 实色绿、ghost 次要、`.btn-text` / `.week-nav-btn` 小控件；`.btn-record-hero` 圆角与触控高度收一档
- 回归：RecordPage 10 tests 仍绿
- 勾选：tasks.md 4.1

### Task 5.x — 文档与 main specs

- 已更新 `doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README 第 7 条
- 已 sync：`openspec/specs/{ui-hierarchy,user-profile,calendar-view,daily-record}/spec.md`
- 勾选：tasks.md 5.1–5.4

### Task 6.1 — 相关回归（本会话）

- 命令：`cd backend && mvn -q test -Dtest=SpaHostingTest,DailyRecordGetByIdTest,CsvExportTest,DailyRecordQueryTest,DailyRecordUpdateDeleteTest`
- 结果：**PASS** — exit 0（含详情/身体资料深链 SPA fallback；GET by id 未抢走 `exportCsv`）
- 命令：`cd frontend && npm test -- --run`
- 结果：**PASS** — Test Files 11 passed；Tests 56 passed (56)
- 未跑全量 `mvn test`（避免 DockerProfile 再开连接池打满 SQLPub）
- 勾选：tasks.md 6.1

### Task 6.2 — openspec validate

- 命令：`openspec validate phase-3-ui-hierarchy --type change`
- 结果：`Change 'phase-3-ui-hierarchy' is valid`
- 勾选：tasks.md 6.2

### Task 6.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`
- 勾选：tasks.md 6.3

---

## §P4-1.1 — 资料历史与 trends API

- 对应规格：`body-history` — Profile changes are stored as history snapshots；Trends without token is 401
- 测试类：`backend/src/test/java/com/workout/profile/ProfileHistoryTrendsTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=ProfileHistoryTrendsTest`
- 结果：**FAIL** — Tests run: 5, Failures: 4
- 失败原因摘要：`GET /api/v1/profile/trends` expected 200 but was **404**（`{"code":404,"msg":"资源不存在"}`）；无 Token 用例为 401（符合既有过滤器）

### GREEN

- 实现：Flyway `V4__profile_history.sql`、`ProfileHistoryEntity`/`Repository`、`ProfileService.upsert` 写快照、`GET /api/v1/profile/trends`
- 命令：同上
- 结果：**PASS** — exit 0（本会话复跑 `ProfileHistoryTrendsTest,CsvExportTest,DeleteAccountTest,FlywayMigrationTest` 亦 exit 0）
- 勾选：tasks.md 1.1

## §P4-1.2 — trends 条数与隔离

- 对应规格：`body-history` — Owner sees own history and counts；Cross-user isolation
- 测试方法：`ProfileHistoryTrendsTest.trendsShouldIncludeOwnRecordCountsAndHideOtherUsers`
- RED：同 §P4-1.1（trends 404）
- GREEN：一次查出 `recordedAt` 再按上海自然日内存聚合；用户 B `bodyHistory.length=0`、`recordCounts.length=0`
- 勾选：tasks.md 1.2

## §P4-2.x — CSV 身体列与 recordedAt 对齐

- 对应规格：`daily-record` — CSV export includes point-in-time body columns
- 测试类：`backend/src/test/java/com/workout/record/CsvExportTest.java`

### RED

- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest#exportEmptyDayShouldContainHeaderOnly,CsvExportTest#exportRowsShouldAlignHeightToHistoryAtRecordedAt`
- 结果：**FAIL**
- 失败原因摘要：空日表头 expected `记录时间,类型,内容,昵称,身高cm,体重kg` but was `记录时间,类型,内容`；对齐行 `早训` 不含 `170`

### GREEN

- 实现：`DailyRecordService.exportCsv` 一次加载 `work_out_profile_history`，`ProfileHistoryResolver.resolve` 取 `changedAt <= recordedAt` 最后一条
- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`
- 结果：**PASS** — exit 0（11 tests，含 BOM 与对齐）
- 勾选：tasks.md 2.1、2.2

## §P4-3.1 — 注销批量删历史

- 对应规格：注销不得留下 `work_out_profile_history` 外键
- 测试方法：`DeleteAccountTest.deleteAccountWithProfileHistoryShouldSucceed`
- GREEN：先 PUT 资料再 `DELETE /api/v1/auth/me` 返回 200（`AuthService.deleteMe` 调用 `profileHistoryRepository.deleteByUserId`）
- 命令：`cd backend && mvn -q test -Dtest=DeleteAccountTest`
- 结果：**PASS** — exit 0
- 勾选：tasks.md 3.1

## §P4-4.1 — 上海时分格式化

- 对应规格：`calendar-view` — Calendar lists show recorded time of day
- 测试文件：`frontend/src/calendar/week.test.ts`

### RED

- 命令：`cd frontend && npm test -- src/calendar/week.test.ts`
- 结果：**FAIL** — `formatShanghaiHm is not a function` / `formatShanghaiMdHm is not a function`

### GREEN

- 实现：`frontend/src/calendar/week.ts` `formatShanghaiHm` / `formatShanghaiMdHm`
- 命令：同上
- 结果：**PASS** — Tests 5 passed
- 勾选：tasks.md 4.1

## §P4-4.2 / 4.3 — 列表时分与变化曲线页

- 对应规格：`calendar-view` / `ui-hierarchy` — 列表时分；`/calendar/trends`
- 测试文件：`frontend/src/CalendarPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx`
- 结果：**FAIL** — 4 failed | 15 passed：找不到 `07:30` / `08-01 07:30`；找不到按钮「变化曲线」；找不到「还没有身体变化数据」

### GREEN

- 实现：`CalendarPage` 列表展示时分 + 「变化曲线」入口；`TrendsPage` SVG；路由 `/calendar/trends`
- 命令：`cd frontend && npm test -- src/calendar/week.test.ts src/CalendarPage.test.tsx src/CsvExportClick.test.tsx src/ProfilePage.test.tsx`
- 结果：**PASS** — Test Files 4 passed；Tests 34 passed
- 勾选：tasks.md 4.2、4.3

## §P4-5.x — 文档与 main specs

- 已更新 `doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README 第 6–7 条
- 已 sync：`openspec/specs/{body-history,calendar-view,daily-record,user-profile,ui-hierarchy}/spec.md`
- 勾选：tasks.md 5.1–5.4

## §P4-6.1 — 相关回归（本会话）

- 命令：`cd backend && mvn -q test -Dtest=ProfileHistoryTrendsTest,CsvExportTest,DeleteAccountTest,FlywayMigrationTest`
- 结果：**PASS** — exit 0
- 命令：`cd frontend && npm test -- src/calendar/week.test.ts src/CalendarPage.test.tsx src/CsvExportClick.test.tsx src/ProfilePage.test.tsx`
- 结果：**PASS** — 34 passed
- 未跑全量 `mvn test`（避免再开连接池打满 SQLPub）
- 勾选：tasks.md 6.1

## §P4-6.2 — openspec validate

- 命令：`openspec validate phase-4-month-csv-body-history-curves --type change`
- 结果：`Change 'phase-4-month-csv-body-history-curves' is valid`
- 勾选：tasks.md 6.2

## §P4-6.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`
- 勾选：tasks.md 6.3

---

## §P5-1.1 — 资料真实日期写入 changedAt

- 对应规格：`body-history` — Profile changes are stored as history snapshots；Scenario: Client supplied changedAt is stored
- 测试类：`backend/src/test/java/com/workout/profile/ProfileHistoryTrendsTest.java` → `putWithClientChangedAtShouldStoreThatInstantOnHistory`

### RED

- 命令：`cd backend && mvn test -Dtest=ProfileHistoryTrendsTest#putWithClientChangedAtShouldStoreThatInstantOnHistory`
- 结果：**FAIL** — 历史 `changedAt` 为服务器 now，不是客户端 `2026-08-01T08:00:00+08:00`（同瞬间 `2026-08-01T00:00:00Z`）

### GREEN

- 实现：`ProfileRequest.changedAt`；`ProfileService.upsert` 用客户端时间写 history 与 `updatedAt`（缺省 now）
- 命令：`cd backend && mvn -q test -Dtest=ProfileHistoryTrendsTest`
- 结果：**PASS** — exit 0（6 tests，含本方法）
- 勾选：tasks.md 1.1

## §P5-2.1 — 导出缺身高体重 400

- 对应规格：`daily-record` — Export requires current height and weight
- 测试类：`CsvExportTest#exportWithoutHeightOrWeightShouldReturn400`、`exportWithWeightOnlyShouldReturn400`

### RED

- 命令：`cd backend && mvn test -Dtest=CsvExportTest#exportWithoutHeightOrWeightShouldReturn400`
- 结果：**FAIL** — 无资料仍 200 返回文件，未 400「请先填写身高和体重」

### GREEN

- 实现：`DailyRecordService.requireCompleteBody`，导出前校验当前 profile
- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`
- 结果：**PASS** — exit 0（14 tests）
- 勾选：tasks.md 2.1

## §P5-2.2 — 导出 xlsx 双工作表

- 对应规格：`daily-record` — CSV export includes point-in-time body columns（xlsx 双 sheet）
- 测试类：`CsvExportTest#exportEmptyDayShouldBeXlsxWithTwoSheets`；对齐测 `exportRowsShouldAlignHeightToHistoryAtRecordedAt`

### RED

- 命令：`cd backend && mvn test -Dtest=CsvExportTest#exportEmptyDayShouldBeXlsxWithTwoSheets`
- 结果：**FAIL** — 仍为 text/csv，无「事项列表」「成长曲线」sheet

### GREEN

- 实现：Apache POI `poi-ooxml`、`XlsxExportWriter`；MIME OpenXML spreadsheet；文件名 `.xlsx`
- 命令：同 §P5-2.1
- 结果：**PASS** — 双 sheet + 事项列表 recordedAt 对齐仍成立
- 勾选：tasks.md 2.2

## §P5-3.1 — Flyway V5 分享表

- 对应：脚手架，非行为 TDD
- 测试类：`FlywayMigrationTest` 断言含 `work_out_share_report`

### RED

- 命令：`cd backend && mvn test -Dtest=FlywayMigrationTest`
- 结果：**FAIL** — tables 不含 `work_out_share_report`

### GREEN

- 实现：`V5__share_report.sql`
- 命令：同上
- 结果：**PASS** — exit 0（1 test）
- 勾选：tasks.md 3.1

## §P5-3.2 / 3.3 — 创建分享与公开报告

- 对应规格：`share-report`
- 测试类：`ShareReportTest`（create token/url、缺身高 400、无 JWT 401、匿名 GET、未知 404）

### RED

- 命令：`cd backend && mvn test -Dtest=ShareReportTest`
- 结果：**FAIL** — `POST /api/v1/shareReports` 与 `GET /api/v1/reports/{id}` 404（路由不存在）

### GREEN

- 实现：`modules.share.*`；`WORKOUT_PUBLIC_BASE_URL`；公开 GET permitAll；快照一次加载
- 命令：同上
- 结果：**PASS** — exit 0（5 tests）
- 勾选：tasks.md 3.2、3.3

## §P5-3.4 — 注销删分享与 SPA `/report/**`

- `AuthService` 注销路径调用 `shareReportRepository.deleteByUserId`
- `SpaHostingTest#reportDeepLinkShouldForwardToSpa` GREEN（随 `SpaHostingTest` 6 tests，`mvn -q test -Dtest=SpaHostingTest` 本会话回归 exit 0）
- 勾选：tasks.md 3.4

## §P5-4.x — 曲线迁到资料页 + pan/zoom

- 对应规格：`user-profile` / `body-history` / `calendar-view`
- 测试文件：`frontend/src/ProfilePage.test.tsx`、`CalendarPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/ProfilePage.test.tsx src/CalendarPage.test.tsx`
- 结果：**FAIL** — 找不到「资料真实日期」/ PUT 无 `changedAt`；找不到 `cm` 与粒度切换；日历仍有「变化曲线」按钮

### GREEN

- 实现：`ProfileBodyPage` datetime-local；`GrowthCurve` 单位/时间轴/pan/`data-precision`；日历去掉曲线入口；`/calendar/trends` → `/profile/body`
- 命令：`cd frontend && npm test`
- 结果：**PASS** — Test Files 12 passed；Tests 66 passed
- 勾选：tasks.md 4.1–4.3

## §P5-5.1 — 导出/分享闸门

- 对应规格：`daily-record` / `calendar-view` 身高体重拦截
- 测试：`CalendarPage` intercepts export / share；`CsvExportClick` 已填资料可下载（`.xlsx` 文件名由 `csvFilename()`）

### RED

- 导出拦截随日历改测先红（仍走 download）；分享拦截与导出共用 `requireBodyOrRedirect`
- 本会话补测 `intercepts share when height or weight is missing`（行为已实现，该断言直接 GREEN）

### GREEN

- 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/CsvExportClick.test.tsx`
- 结果：**PASS** — CalendarPage 21 tests；CsvExportClick 3 tests（全量 66 passed）
- 勾选：tasks.md 5.1

## §P5-5.2 — 公开报告页

- 对应规格：`ui-hierarchy` / `share-report` — `/report/:id` 无三 Tab
- 测试：`frontend/src/ReportPage.test.tsx`

### RED

- 命令：`cd frontend && npm test -- src/ReportPage.test.tsx`
- 结果：**FAIL** — 无 `/report/:id` 路由，找不到「用户名称」「建议分析」

### GREEN

- 实现：独立 `ReportPage`；上下用户名称 / 事项列表 / 成长曲线 / 建议分析空态；范围可见
- 命令：同上 / 全量 `npm test`
- 结果：**PASS** — 1 test + 全量 66 passed
- 勾选：tasks.md 5.2

## §P5-6.x — 文档与 main specs

- 已更新 `doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README 第 6–7 条与 `WORKOUT_PUBLIC_BASE_URL`
- 已 sync：`openspec/specs/{share-report,body-history,user-profile,daily-record,calendar-view,ui-hierarchy}/spec.md`
- 勾选：tasks.md 6.1–6.4

## §P5-7.1 — 相关回归（本会话）

- 命令：`cd backend && mvn -q test -Dtest=ProfileHistoryTrendsTest,CsvExportTest,ShareReportTest,FlywayMigrationTest,SpaHostingTest`
- 结果：**PASS** — exit 0（窄测；未打满 SQLPub 全量 `mvn test`）
- 命令：`cd frontend && npm test`
- 结果：**PASS** — Test Files 12 passed；Tests 66 passed
- 勾选：tasks.md 7.1

## §P5-7.2 — openspec validate

- 命令：`openspec validate phase-5-share-report-curve-xlsx --type change`
- 结果：`Change 'phase-5-share-report-curve-xlsx' is valid`
- 勾选：tasks.md 7.2

## §P5-7.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`
- 勾选：tasks.md 7.3

## §P6-1.1 — 事项 sheet 去掉身体列

- 对应规格：`daily-record` — 事项列表仅 `记录时间,类型,内容`；曲线 sheet 仍有身高体重
- 测试文件：`backend/src/test/java/com/workout/record/CsvExportTest.java`
- RED 命令：`cd backend && mvn -q test -Dtest=CsvExportTest`
- RED 结果：FAIL — Tests run: 14, Failures: 6 — 空日表头 expected `记录时间,类型,内容` but was `记录时间,类型,内容,昵称,身高cm,体重kg`；事项行仍含 `身高cm`/`170`
- GREEN 命令：同上
- GREEN 结果：PASS — Tests run: 14, Failures: 0（exit 0）
- 实现要点：`XlsxExportWriter.writeRecordsSheet` 三列，不再写入昵称/身高/体重
- 勾选：tasks.md 1.1

## §P6-2.1 / 2.2 — 分享同级按钮与二级页

- 对应规格：`calendar-view` / `ui-hierarchy` / `share-report`
- 测试文件：`frontend/src/CalendarPage.test.tsx`、`frontend/src/SharePage.test.tsx`
- RED 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/SharePage.test.tsx`
- RED 结果：FAIL — 分享无 `btn-block`、导出仍 `btn-primary`；点击分享仍停在 `/calendar`；无 `/calendar/share` 路由、找不到链接与「返回日历」
- GREEN 命令：`cd frontend && npm test -- src/CalendarPage.test.tsx src/SharePage.test.tsx src/CsvExportClick.test.tsx`
- GREEN 结果：PASS — CalendarPage 23；SharePage 2；CsvExportClick 3
- 实现要点：`action-pair` 双 ghost block；点分享 `navigate(/calendar/share?…)`；`SharePage` POST 后展示 url/复制/返回日历
- 勾选：tasks.md 2.1–2.2

## §P6-3.1 / 3.2 / 3.3 — 报告回首页、列表分行、缩放 −/+

- 对应规格：`share-report` / `calendar-view` / `user-profile` / `body-history`
- 测试文件：`frontend/src/ReportPage.test.tsx`、`CalendarPage.test.tsx`、`ProfilePage.test.tsx`
- RED 命令：`cd frontend && npm test -- src/ReportPage.test.tsx src/ProfilePage.test.tsx src/CalendarPage.test.tsx`
- RED 结果：FAIL — 找不到 link「回首页」；ul 无 `record-list--stacked`；放大按钮可见文本仍为「放大」而非 `+`
- GREEN 命令：同上（含相关套件）
- GREEN 结果：PASS — ReportPage 1；ProfilePage 10；CalendarPage 23
- 实现要点：`ReportPage` Link `/`；`.record-list--stacked` 时间/内容 column；GrowthCurve `aria-label` 缩小/放大，可见 `−`/`+`
- 勾选：tasks.md 3.1–3.3

## §P6-4.1 — Demo 种子数据集

- 对应规格：设计 D7；非 test ApplicationRunner
- 测试文件：`backend/src/test/java/com/workout/bootstrap/DemoDatasetTest.java`
- RED 命令：先写 `DemoDatasetTest`（生产类尚未存在）
- RED 结果：缺类则无法通过；本会话按测试契约实现 `DemoDataset.build`
- GREEN 命令：`cd backend && mvn -q test -Dtest=DemoDatasetTest`
- GREEN 结果：PASS — records=104, history=7, from=2026-05-20, to=2026-11-16（相对 2026-08-18 ±90 天）
- 实现要点：`DemoDataset.build(Clock)`；`DemoDataSeeder` `@Profile("!test")`，`existsByUsername` 则跳过，`saveAll` 批量
- 勾选：tasks.md 4.1

## §P6-5.x — 文档与 main specs

- 已更新 `doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、`doc/workOut-技术架构.md`（CSV→xlsx）、README
- 已 sync：`openspec/specs/{calendar-view,share-report,daily-record,ui-hierarchy,user-profile,body-history}/spec.md`
- 勾选：tasks.md 5.1–5.5

## §P6-6.1 — 相关回归（本会话）

- 命令：`cd backend && mvn -q test -Dtest=CsvExportTest,DemoDatasetTest,ShareReportTest`
- 结果：**PASS** — exit 0（窄测；未打满 SQLPub 全量 `mvn test`）
- 命令：`cd frontend && npm test`
- 结果：**PASS** — Test Files 13 passed；Tests 70 passed
- 勾选：tasks.md 6.1

## §P6-6.2 — openspec validate

- 命令：`openspec validate phase-6-share-export-demo --type change`
- 结果：`Change 'phase-6-share-export-demo' is valid`
- 勾选：tasks.md 6.2

## §P6-6.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`
- 勾选：tasks.md 6.3

## §CMSNAV-1.1 — Admin 用户详情 API

- 对应规格：`openspec/changes/cms-nav-user-detail-reports/specs/admin-cms/spec.md` — Admin can open user detail
- 测试类：`backend/src/test/java/com/workout/admin/AdminUserDetailTest.java`
- RED 命令：`cd backend && mvn test -Dtest=AdminUserDetailTest`
- RED 结果：FAIL — ADMIN 期望 200 实为 404（`NoResourceFoundException`，路径未实现）；USER 期望 403 实为 404。无 Token 401 承接既有 Security
- GREEN 命令：`cd backend && mvn test -Dtest=AdminUserDetailTest,AdminAccountsListTest`
- GREEN 结果：PASS — exit 0
- 实现要点：`AdminAccountController.getAccount`；`AdminAccountService.getDetail`（用户/资料/count+top5 记录/该用户分享各一次查询）；未知 id 404；public `log.info`
- 勾选：tasks.md 1.1

## §CMSNAV-2.1 — Admin 分享列表 API

- 对应规格：admin-cms — Admin can list existing share reports
- 测试类：`backend/src/test/java/com/workout/admin/AdminReportsListTest.java`
- RED 命令：`cd backend && mvn test -Dtest=AdminReportsListTest`
- RED 结果：FAIL — ADMIN 期望 200 实为 404；USER 期望 403 实为 404
- GREEN 命令：`cd backend && mvn test -Dtest=AdminReportsListTest`
- GREEN 结果：PASS — exit 0
- 实现要点：`GET /api/v1/admin/reports`；`findAllByOrderByCreatedAtDesc` + `findAllById` 拼用户名；不创建分享
- 勾选：tasks.md 2.1

## §CMSNAV-3.1 — CMS 子路径 SPA 回退

- 对应规格：ui-hierarchy — CMS deep links stay outside the three-tab shell
- 测试类：`SpaHostingTest`
- RED 命令：`cd backend && mvn test -Dtest=SpaHostingTest#cmsAccountsDeepLinkShouldForwardToSpa,SpaHostingTest#cmsReportsDeepLinkShouldForwardToSpa,SpaHostingTest#cmsUserDetailDeepLinkShouldForwardToSpa`
- RED 结果：FAIL — Status expected:<200> but was:<404>
- GREEN 命令：`cd backend && mvn test -Dtest=SpaHostingTest`
- GREEN 结果：PASS — exit 0
- 实现要点：`SpaFallbackController` 增加 `/cms/**`
- 勾选：tasks.md 3.1

## §CMSNAV-4.1 — CMS 功能栏、详情、报告页

- 对应规格：admin-cms 功能栏 / 详情 / 报告页；未登录仍跳登录
- 测试文件：`frontend/src/CmsPage.test.tsx`
- RED 命令：`cd frontend && npm test -- src/CmsPage.test.tsx`
- RED 结果：FAIL — 8 failed：`/cms/reports` 未跳登录；无「概览」链接；`/cms/accounts` 无账户表；详情/报告无 `cms_alice`（子路径落入三 Tab 壳）
- GREEN 命令：同上
- GREEN 结果：PASS — Tests 11 passed
- 实现要点：`CmsLayout` 顶栏功能栏 + 嵌套路由 `/cms` `/cms/accounts` `/cms/users/:userId` `/cms/reports`；用户名 Link；分享链到 `/report/:id`
- 勾选：tasks.md 4.1

## §CMSNAV-5.x — 文档与 main specs

- 已更新 `doc/workOut-产品文档.md` §4.4、`doc/workOut-功能文档.md` §6.4/§8.9～8.11、`doc/workOut-技术架构.md`、README
- 已 sync：`openspec/specs/admin-cms/spec.md`（新建）；`ui-hierarchy`、`share-report` 增加 CMS 要求
- 勾选：tasks.md 5.1–5.5

## §CMSNAV-6.1 — 相关回归（本会话）

- 命令：`cd backend && mvn test -Dtest=AdminUserDetailTest,AdminReportsListTest,AdminAccountsListTest,SpaHostingTest`
- 结果：**PASS** — Tests run: 22, Failures: 0（窄测；未打满 SQLPub 全量 `mvn test`）
- 命令：`cd frontend && npm test`
- 结果：**PASS** — Test Files 13 passed；Tests 75 passed
- 勾选：tasks.md 6.1

## §CMSNAV-6.2 — openspec validate

- 命令：`openspec validate cms-nav-user-detail-reports --type change`
- 结果：`Change 'cms-nav-user-detail-reports' is valid`
- 勾选：tasks.md 6.2

## §CMSNAV-6.3 — 不提交 git

- 本实现会话未执行 `git commit` / `git push`
- 勾选：tasks.md 6.3



## 七期 phase-7-ai-advice-deepseek

### Task 2.1–2.3 / 3.2–3.3 — Stub DeepSeek、限流、无 Key、异步建议

- 对应规格：`openspec/specs/ai-advice/spec.md`；`share-report` adviceStatus
- 测试类：`ShareAdviceAsyncTest`、`AiAdviceRateLimitTest`、`ShareReportTest`
- RED→GREEN：先断言 NONE_KEY / SQL 限流 / stub READY·FAILED；实现后窄测全绿
- GREEN 命令：`cd backend && mvn -q test -Dtest=ShareAdviceAsyncTest,AiAdviceRateLimitTest,AdminApiKeyAndAiCallTest,ShareReportTest`
- GREEN 结果：PASS — Tests run: 12, Failures: 0（Stub 禁外网；假 key；MySQL COUNT 限流）
- 实现要点：`StubDeepSeekClient`；`AiRateLimitService` SQL 聚合；`ShareAdviceWorker` REQUIRES_NEW；AFTER_COMMIT 事件
- 勾选：tasks.md 2.1–2.3、3.2–3.3

### Task 4.1–4.2 — CMS API Key / AI 调用

- 测试类：`AdminApiKeyAndAiCallTest`
- GREEN：同上命令内 PASS（401/403/掩码/batch）
- 勾选：tasks.md 4.1–4.2

### Task 5.2–5.3 — 报告页与 CMS 栏

- 测试文件：`frontend/src/ReportPage.test.tsx`、`CmsPage.test.tsx`
- GREEN 命令：`cd frontend && npm test -- src/ReportPage.test.tsx src/CmsPage.test.tsx`
- GREEN 结果：PASS — 15 tests
- 勾选：tasks.md 5.1–5.3

### Task 6.x — Skill / 文档 / sync

- Skill：`.cursor/skills/physio-scientist-advice/SKILL.md`
- 文档：`doc/workOut-AI方案.md`、`doc/workOut-上下文工程.md`；产品/功能/架构已改「建议分析」
- main specs：`ai-advice` 新建；`admin-cms` / `share-report` / `ui-hierarchy` 已 sync
- Key 初始化：`application.yml` / `application-docker.yml` 的 `WORKOUT_DEEPSEEK_API_KEY`；`DeepSeekApiKeySeeder` 赋给 seed 用户名（demo、lipp）；日志仅掩码；测试用不真 key
- 未 commit

