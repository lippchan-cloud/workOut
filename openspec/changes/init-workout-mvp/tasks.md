# Tasks: init-workout-mvp

> **TDD 强制规则（每个实现子任务）：**  
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。  
> 禁止先写生产代码再补测试。  
>
> **验证门禁（高于「有测试类」）：**  
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)  
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)  
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**  
> - TDD ≠ 只写 `*Test.java`；必须可审计的红→绿证据。

## 1. Bootstrap 工程与数据库

- [x] 1.1 创建 `backend/` Spring Boot 3.x Maven 工程骨架与 `WorkOutApplication`；添加依赖：Web、Validation、JPA、Security、Flyway、JWT（jjwt）、Testcontainers（或约定 H2 test profile）
- [x] 1.2 **TDD** 写失败测试 `FlywayMigrationTest`（或 `SchemaSmokeTest`）：断言表 `user` / `daily_record` / `profile` 存在 → 添加 Flyway `V1__init.sql`（含 `user_id` FK/索引、utf8mb4）→ 测试转绿
- [ ] 1.3 创建 `frontend/` Vite + React + TypeScript + React Router；配置 Vitest + Testing Library
- [ ] 1.4 配置前端 build 产物复制到 `backend/src/main/resources/static`（插件或脚本）；文档化 CLI：`./mvnw spring-boot:run`（或根目录包装脚本）
- [ ] 1.5 **TDD** 写失败测试 `SpaHostingTest`：`GET /` 返回 HTML；`GET /calendar` 不 404 到错误 API → 实现静态托管与 SPA fallback → 转绿
- [x] 1.6 统一响应体 `ApiResponse`、异常处理（400/401/500）、`requestId`/`timestamp`；用一个最小 MockMvc 测试锁定 envelope 形状

## 2. User Auth（后端优先）

- [x] 2.1 **TDD** `AuthRegisterTest`：注册成功返回 token；重复用户名 400；非法密码 400 → 实现 `POST /api/v1/auth/register` + BCrypt → 转绿
- [ ] 2.2 **TDD** `AuthLoginTest`：正确密码返回 token；错误密码通用中文错误且无 token → 实现 `POST /api/v1/auth/login` → 转绿
- [ ] 2.3 **TDD** `JwtAuthFilterTest`：无 Token 访问受保护桩接口 401；有效 Token 200；`/api/v1/auth/**` 放行 → 实现 JWT 签发/解析与 Security 配置 → 转绿
- [ ] 2.4 **TDD** 前端 `authRedirect.test.tsx`：无 token 点击日历 Tab → 进入 `/login?redirect=/calendar` → 实现 AuthContext + 守卫 → 转绿
- [ ] 2.5 实现 `/login`、`/register` 页面与 API client（存 `localStorage`）；**TDD** 登录表单校验空用户名提示；401 拦截清 token 跳转登录

## 3. App Shell

- [ ] 3.1 **TDD** `AppShell.test.tsx`：根路径渲染三个 Tab 文案「记录」「日历」「我的」→ 实现布局与底部导航 → 转绿
- [ ] 3.2 **TDD** 已登录时可切换 `/record`、`/calendar`、`/profile` 而不被踢出 → 实现路由 → 转绿
- [ ] 3.3 未登录点击任一 Tab 均带正确 `redirect`（覆盖记录/日历/我的三条断言）

## 4. Daily Record

- [ ] 4.1 **TDD** `DailyRecordCreateTest`：认证用户创建 CONSUME 成功且 DB `user_id` 正确；空内容 400「请填写内容」；501 字 400 → 实现 `POST /api/v1/dailyRecords` → 转绿
- [ ] 4.2 **TDD** `DailyRecordIsolationTest`：用户 A 的记录对用户 B 的按日查询不可见 → 查询强制 `userId` 条件 → 转绿
- [ ] 4.3 **TDD** `DailyRecordQueryTest`：同日两条按 `recordedAt` 升序（再 `id`）返回 → 实现 `GET /api/v1/dailyRecords?date=` → 转绿
- [ ] 4.4 **TDD** 前端记录页：保存消耗成功后清空输入；未登录点保存跳转登录 → 实现记录页两表单 → 转绿

## 5. Calendar View

- [ ] 5.1 **TDD** 周计算工具测试：给定周三日期，返回本周一至周日；默认选中今天 → 实现 week utils（周一为一周起始）→ 转绿
- [ ] 5.2 **TDD** `CalendarPage.test.tsx`：渲染周条；空列表展示「这一天还没有记录」→ 接查询 API → 转绿
- [ ] 5.3 **TDD** 列表项 CONSUME 使用绿色 token、INTAKE 使用红色 token（断言 class 或 style）→ 实现样式约定 `#16A34A` / `#DC2626` → 转绿
- [ ] 5.4 上一周/下一周切换行为测试 + 实现

## 6. CSV Export

- [ ] 6.1 **TDD** `CsvExportTest`：有数据导出含 BOM、表头 `记录时间,类型,内容`、类型中文、文件名 `workout-YYYY-MM-DD.csv`、仅当前用户 → 实现 `GET /api/v1/dailyRecords/exportCsv` → 转绿
- [ ] 6.2 **TDD** 无数据仅表头；无 Token 401 → 转绿
- [ ] 6.3 **TDD** 前端点击导出触发下载（可用 mock）；未登录点击跳转登录 → 转绿

## 7. User Profile

- [ ] 7.1 **TDD** `ProfileUpsertTest`：PUT 保存后 GET 回显；身高越界 400；用户 B 读不到 A → 实现 `GET/PUT /api/v1/profile` → 转绿
- [ ] 7.2 **TDD** 前端资料页回填与保存成功提示；退出登录清除 token → 实现「我的」页 → 转绿

## 8. 端到端验收与文档收尾

- [ ] 8.1 按功能文档 T00–T13 手工走通一遍并记录结果（可附 `doc/验收记录.md`）
- [ ] 8.2 更新根 `README.md`：启动步骤、MySQL 配置、默认端口、测试命令（`mvn test`、`npm test`）
- [ ] 8.3 确认 `openspec validate init-workout-mvp` 仍通过；全部 tasks 勾选后准备 `/opsx:archive`

## 9. 建议提交节奏（实现阶段）

- [ ] 9.1 每完成一组 TDD（如 2.1–2.3）单独 commit，message 使用 conventional commits（`feat:` / `test:`）
- [ ] 9.2 不在未确认前 push；不把密钥写入仓库
