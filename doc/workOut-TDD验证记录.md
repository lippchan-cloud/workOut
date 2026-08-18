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
| 其余 | — | 未开始 | 未开始 | — | 否 |

---

## §1.1 — Backend 脚手架（非行为 TDD）

- 对应：`openspec/.../tasks.md` 1.1
- 说明：创建 `backend/` Spring Boot 3.3.5、依赖 Web/Validation/JPA/Security/Flyway/MySQL/H2/JJWT、入口 `WorkOutApplication`
- 验证：`cd backend && mvn -q test -Dtest=FlywayMigrationTest` 能启动上下文（见 §1.2）
- 备注：脚手架本身无 Scenario 断言；业务行为不得借本任务偷跑

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
