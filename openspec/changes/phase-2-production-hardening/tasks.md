# Tasks: phase-2-production-hardening

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `phase-2-production-hardening`. Override with `/opsx:apply <other>`.

## 1. Role column + bootstrap ADMIN (backend)

- [ ] 1.1 **TDD** 改写/新增 `backend/src/test/java/com/workout/admin/AdminAccountsListTest.java`：无 Token `GET /api/v1/admin/accounts` 期望 **401**（不再 200）。RED → Flyway V3 `work_out_user.role` + `SecurityConfig` 去掉 CMS `permitAll` → GREEN。写入验证记录
- [ ] 1.2 **TDD** 普通 USER JWT 调账户列表期望 **403**；配置 `workout.admin.usernames` 中的用户名注册后 `data.role=ADMIN` 且列表 200。RED → `UserEntity.role`、`AuthTokenResponse.role`、注册/登录提升、`ForbiddenException`、Admin 服务校验 → GREEN

## 2. CMS 前端收口

- [ ] 2.1 **TDD** `frontend/src/CmsPage.test.tsx`：无 token 打开 `/cms` 跳转 `/login?redirect=/cms`；登录页 **没有** 「后台管理」链接。RED → `CmsPage`/`LoginPage`/`App` → GREEN
- [ ] 2.2 **TDD** USER 打开 `/cms` 见非管理员中文提示且不拉出账户表；ADMIN 可见列名且无 `passwordHash`。RED → 存 `workout_role`、CMS 守卫 → GREEN

## 3. 记录 PUT / DELETE (backend)

- [ ] 3.1 **TDD** `backend/src/test/java/com/workout/record/DailyRecordUpdateDeleteTest.java`：所有者 PUT 成功；空内容 400「请填写内容」。RED → `PUT /api/v1/dailyRecords/{id}` → GREEN
- [ ] 3.2 **TDD** 所有者 DELETE 后列表不再包含；跨用户 PUT/DELETE 404 且原记录仍在。RED → `DELETE` 逻辑删除 + 按 `id+userId` 一次查询 → GREEN

## 4. 日历加载态与改删补记 (frontend)

- [ ] 4.1 **TDD** `frontend/src/CalendarPage.test.tsx`：请求进行中显示加载文案、不显示空态；失败显示可重试、不显示空态；成功空列表才显示「这一天还没有记录」。RED → 去掉 `catch(() => setList([]))` → GREEN
- [ ] 4.2 **TDD** 列表项有编辑/删除；删除先确认再调 DELETE；编辑进入表单带回内容。RED → Calendar + Record 编辑路由 → GREEN
- [ ] 4.3 **TDD** 选中日「补记」进入带 `date` 的类型页/表单，datetime 使用该日。RED → 查询参数回填 → GREEN

## 5. 表单校验、保存去向、401 草稿

- [ ] 5.1 **TDD** `frontend/src/RecordPage.test.tsx`：空内容即时「请填写内容」且不发创建请求；保存成功出现「再记一条」「回日历」。RED → RecordPage → GREEN
- [ ] 5.2 **TDD** 保存遇 401 跳转登录且 redirect 回表单；登录后草稿内容恢复。RED → sessionStorage 草稿 + client/form → GREEN

## 6. 改密与注销

- [ ] 6.1 **TDD** `backend/src/test/java/com/workout/auth/ChangePasswordTest.java`：正确旧密改成功，新密可登录旧密失败；错误旧密 400。RED → `PUT /api/v1/auth/password` → GREEN
- [ ] 6.2 **TDD** `backend/src/test/java/com/workout/auth/DeleteAccountTest.java`：注销后无法登录，本人记录/资料不可再查。RED → `DELETE /api/v1/auth/me` 批量删（禁止 N+1）→ GREEN
- [ ] 6.3 **TDD** `frontend/src/ProfilePage.test.tsx`：账号区与身体数据分区；改密控件存在；注销需确认后调删除 API 并清 token。RED → ProfilePage → GREEN

## 7. 门禁收尾

- [ ] 7.1 跑相关回归：Admin/Record/Auth 后端测试 + Cms/Calendar/Record/Profile 前端测试；命令与结果写入验证记录
- [ ] 7.2 `openspec validate --change phase-2-production-hardening` 通过
- [ ] 7.3 不提交 git、不纳入 `backend/target/**`；不拆多环境 yml、不外置全部 secret
