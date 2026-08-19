# Tasks: c-end-profile-email-reports

> **TDD：** 先写失败测试，再最小实现，再勾选。
>
> Using change: `c-end-profile-email-reports`. Override with `/opsx:apply <other>`.

## 1. 数据与发信

- [x] 1.1 Flyway `V8__user_email.sql`：`work_out_user.email` 可空唯一；`work_out_email_code` 表
- [x] 1.2 `EmailSender` + Logging 实现；test profile `CapturingEmailSender`；UserEntity/Repository 增加 email

## 2. 后端会话与邮箱

- [x] 2.1 **TDD** `AuthMeTest`：`GET /api/v1/auth/me` 无 Token 401；有 JWT 返回 username/role/email
- [x] 2.2 **TDD** `EmailBindLoginTest`：4 位发码、绑定、占用冲突、解绑、邮箱验证码登录、未绑定失败、绑定后用户名密码仍可登录

## 3. 后端本人报告列表

- [x] 3.1 **TDD** `MyShareReportsListTest`：无 Token 401；本人 list 含 token；他人看不到

## 4. 前端顶栏、登录、我的

- [x] 4.1 **TDD** `AppShell`：未登录顶栏「登录」带 redirect；已登录显示用户名
- [x] 4.2 **TDD** 登录页邮箱验证码通道；账号安全绑定/解绑；确认弹窗覆盖退出/改密/注销/绑定/解绑
- [x] 4.3 **TDD** 「我的」含报告记录；`/profile/reports` 列表或空态

## 5. 文档与收尾

- [x] 5.1 产品/功能文档去掉「本期不做邮箱」，补 C 端报告记录与顶栏账号
- [x] 5.2 跑相关测试；`openspec validate`；不 commit
