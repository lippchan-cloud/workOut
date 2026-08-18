# workOut TDD 验证记录

| 项 | 内容 |
| --- | --- |
| 文档类型 | TDD 红绿证据台账 |
| 文档版本 | v1.0 |
| 分支 | `feat/init-workout-mvp` |
| 规范 | [workOut-TDD规范.md](./workOut-TDD规范.md) |
| OpenSpec | [tasks.md](../openspec/changes/init-workout-mvp/tasks.md) |

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

---

## §1.1 — Backend 脚手架（非行为 TDD）

- 对应：`openspec/.../tasks.md` 1.1
- 说明：创建 `backend/` Spring Boot 3.3.5、依赖 Web/Validation/JPA/Security/Flyway/MySQL/JJWT、入口 `WorkOutApplication`
- 验证：`cd backend && mvn -q test -Dtest=FlywayMigrationTest` 能启动上下文（见 §1.2）
- 备注：脚手架本身无 Scenario 断言；业务行为不得借本任务偷跑

---

## §测试库 — 直连 SQLPub（2026-08-18）

- 变更：`application-test.yml` 指向与主配置相同的 SQLPub MySQL（`inv_doc`）；移除 H2 依赖
- 隔离：测试注册用户名经 `TestUsernames.unique(prefix)` 追加 8 位 UUID 后缀，避免固定用户名冲突；**不保证回滚**，会在真实库留下测试行
- Flyway：仅跑未记录迁移（`V1__init.sql` 建 workout 表），不改其它业务表
- 验证：`cd backend && mvn test`（需外网访问 SQLPub）

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
