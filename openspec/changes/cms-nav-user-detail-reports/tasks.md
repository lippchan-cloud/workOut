# Tasks: cms-nav-user-detail-reports

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `cms-nav-user-detail-reports`. Override with `/opsx:apply <other>`.

## 1. 后端用户详情 API

- [x] 1.1 **TDD** `backend/src/test/java/com/workout/admin/AdminUserDetailTest.java`：无 Token `GET /api/v1/admin/accounts/{id}` 401；USER JWT 403；ADMIN 200 含 username/role/createdAt/nickname/heightCm/weightKg/recordCount/recentRecords/shares，JSON 不含 `passwordHash`；未知 id 404。RED → `AdminAccountController` + Service 详情（批量/单次查询，禁止 N+1）→ GREEN。public 方法 `log.info`。写入验证记录

## 2. 后端分享报告列表 API

- [x] 2.1 **TDD** `backend/src/test/java/com/workout/admin/AdminReportsListTest.java`：无 Token `GET /api/v1/admin/reports` 401；USER 403；ADMIN 200 且 list 含 token/id、userId、username、from、to、createdAt。RED → 批量查分享 + `findByIdIn` 用户名 → GREEN。写入验证记录

## 3. SPA 深链

- [x] 3.1 **TDD** `SpaHostingTest`：`GET /cms/accounts`、`GET /cms/reports`、`GET /cms/users/1` forward index.html。RED → `SpaFallbackController` `/cms/**` → GREEN

## 4. 前端 CMS 功能栏、详情、报告

- [x] 4.1 **TDD** `frontend/src/CmsPage.test.tsx`：未登录打开 `/cms` 仍跳 `/login?redirect=/cms`；未登录 `/cms/reports` 跳 `/login?redirect=/cms/reports`。ADMIN 在 `/cms/accounts` 见功能栏「概览 / 账户列表 / 用户详情 / 报告」且账户列表高亮；点用户名进 `/cms/users/:id` 见用户名、角色、资料、记录摘要。`/cms/reports` 见分享用户名与 `/report/:id` 链接。USER 仍见非管理员提示。RED → `CmsLayout` + 嵌套路由 + 详情/报告页 → GREEN。写入验证记录

## 5. 文档与 specs sync

- [x] 5.1 更新 `doc/workOut-产品文档.md` CMS 章节
- [x] 5.2 更新 `doc/workOut-功能文档.md` CMS 章节
- [x] 5.3 必要时更新 `doc/workOut-技术架构.md`
- [x] 5.4 README 相关句
- [x] 5.5 按 openspec-sync-specs 把 delta 写入 `openspec/specs/`

## 6. 回归与收尾

- [x] 6.1 跑相关前后端测试并写入验证记录；全量 `mvn test` 若打满 SQLPub 则窄测并说明
- [x] 6.2 `openspec validate --change cms-nav-user-detail-reports`
- [x] 6.3 **不 commit、不 push**
