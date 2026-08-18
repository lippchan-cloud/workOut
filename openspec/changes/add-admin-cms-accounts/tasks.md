## 1. Backend accounts API (TDD)

- [x] 1.1 写失败测试 `backend/src/test/java/com/workout/admin/AdminAccountsListTest.java`：无 Token `GET /api/v1/admin/accounts` 期望 200 且列出刚注册用户与资料、JSON 不含 `passwordHash`；无资料用户字段为 null。跑 RED，写入 `doc/workOut-TDD验证记录.md`
- [x] 1.2 最小实现 `modules.admin`（Controller/Service/Response）、`ProfileRepository.findByUserIdIn` 批量查询、`SecurityConfig` 仅放行该 GET；public 方法 `log.info`。同一命令 GREEN，补验证记录后勾选
- [x] 1.3 写/扩测试证明 `GET /api/v1/profile` 与 `GET /api/v1/dailyRecords?date=2026-08-18` 无 Token 仍 401。RED（若尚未覆盖）→ GREEN。可放在 `AdminAccountsListTest` 或扩展 `JwtAuthFilterTest`

## 2. SPA 深链与前端 CMS 页 (TDD)

- [x] 2.1 扩展 `SpaHostingTest`：`GET /cms` forward 到 index.html。RED → `SpaFallbackController` 增加 `/cms` → GREEN
- [x] 2.2 写失败测试 `frontend/src/CmsPage.test.tsx`：未登录打开 `/cms` 可见临时鉴权横幅与列名（用户名/创建时间/昵称/身高/体重），不出现 `passwordHash`；mock 列表渲染用户名与昵称。RED → 实现独立 `CmsPage` + `App.tsx` 路由 → GREEN
- [x] 2.3 写失败测试：登录页有「后台管理」链接指向 `/cms`。RED → `LoginPage` 入口 → GREEN
- [x] 2.4 补测列「用户ID」与加载/空态/错误态：先红后绿，写入验证记录

## 3. 门禁收尾

- [x] 3.1 跑相关回归：`AdminAccountsListTest`、`JwtAuthFilterTest`、`SpaHostingTest`、前端 `CmsPage.test.tsx` 与登录相关测；命令与结果写入验证记录
- [x] 3.2 `openspec validate add-admin-cms-accounts` 通过
- [x] 3.3 不提交 git、不纳入 `backend/target/**`
