## Why

一期账本已能记消耗/摄入，但 CMS 账户列表仍对公网无鉴权开放、记录不能改删、日历把加载失败伪装成空列表、表单与「我的」缺少改密/注销。二期目标是「能安全给人用的个人账本」，不是组织/SSO，也不是多环境拆配置。

## What Changes

- **BREAKING** `GET /api/v1/admin/accounts` 与 SPA `/cms` 必须已登录且角色为 `ADMIN`；普通用户 JWT 不得拉全站账户
- **BREAKING** 登录页去掉免登录「后台管理」入口；仅登录后的管理员可见 CMS 入口
- 消耗/摄入记录支持 `PUT` / `DELETE`（按 id、身份只取 JWT、用户隔离）；日历列表每条可改可删，删除二次确认；保存成功后去向清楚
- 日历加载中 / 失败可重试 / 真正空态三套文案；禁止把失败当成「没有记录」
- 日历某天「补记」直达记录表单（可带日期）；表单必填/长度即时校验；401 回登录并尽量保留草稿或 redirect
- 「我的」：改密必做；注销（删本人数据）建议做且危险操作确认；账号区与身体数据分开
- 用户表增加最小角色 `USER` / `ADMIN`；第一个管理员由 yml 配置用户名在注册/登录时提升，不引入完整 IAM
- 密钥可继续留在现有 yml；不拆 dev/stage/prod，不强迫外置全部 secret

## Capabilities

### New Capabilities

- （无 — 二期全部落在既有能力的行为收口与补齐）

### Modified Capabilities

- `admin-cms`: 关闭公网口；CMS API 与页面仅 ADMIN；登录页不再免登录进入
- `daily-record`: 按 id 更新/删除；内容校验与保存后去向；401 草稿/redirect
- `calendar-view`: 加载/失败/空态分离；列表改删；某日补记直达表单
- `user-auth`: 角色字段与管理员引导；改密 API
- `user-profile`: 账号区（改密/注销）与身体数据分开；注销删本人数据

## Impact

- 后端：`work_out_user.role`（Flyway）；`AuthPrincipal`/`JWT` 可带 role；`SecurityConfig` 去掉 CMS permitAll；`AdminAccountController` 鉴权 + ADMIN；`DailyRecordController` PUT/DELETE；`AuthController` 改密；注销级联删本人记录/资料/用户；禁止 N+1
- 前端：`CmsPage` / `LoginPage` / `CalendarPage` / `RecordPage` / `ProfilePage` / `App` / `Auth` / `client.ts`；保持绿耗红食与首页大按钮
- 测试：按 `doc/workOut-TDD规范.md` 先红后绿，证据写入 `doc/workOut-TDD验证记录.md`
- 明确不做：多环境配置、HTTPS/反向代理、组织/SSO、卡路里/动作库/社交/趋势图、外挂 logback、git commit
