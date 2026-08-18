# workOut
记录每日训练内容（按账号隔离）

## 核心功能：
1. 登录 / 注册：用户名 + 密码，JWT；未登录可见导航壳，点 Tab 或业务操作跳转登录。
2. 导航栏第一个，用户可以记录，当日消耗项和当日摄入项。
3. 当日消耗项，是一个输入框，可以记录一条内容，记录日期默认是操作时间，也可以自己更换
4. 当日摄入项，是一个输入框，可以记录一条内容，记录日期默认是操作时间，也可以自己更换
5. 导航栏，第二个，是一个日历，按周显示，默认选中今日
6. 选中日期后，下面有列表，按照时间正序展示，消耗绿色，摄入红色。也可以按日期导出一个csv文件，
7. 我的信息页，可以填充个人基本信息，身高体重；支持退出登录。

## 技术选型
### 前端：
react
### 后端：
springboot
### db
mysql，mysql，连接信息见 [doc/workOut-数据库连接.md](./doc/workOut-数据库连接.md)
### 鉴权
JWT

## 启动方式
前后端不分离，用户可以cli启动

## 文档
- [产品文档](doc/workOut-产品文档.md)
- [功能文档](doc/workOut-功能文档.md)
- [技术架构](doc/workOut-技术架构.md)

## OpenSpec + TDD
- 变更：[`openspec/changes/init-workout-mvp`](openspec/changes/init-workout-mvp/proposal.md)
- 任务清单：[`tasks.md`](openspec/changes/init-workout-mvp/tasks.md)
- **TDD 规范（门禁）：** [`doc/workOut-TDD规范.md`](doc/workOut-TDD规范.md)
- **TDD 验证台账（红绿证据）：** [`doc/workOut-TDD验证记录.md`](doc/workOut-TDD验证记录.md)
- 实现计划索引：[`docs/superpowers/plans/2026-08-18-init-workout-mvp.md`](docs/superpowers/plans/2026-08-18-init-workout-mvp.md)
- 校验：`openspec validate init-workout-mvp`
- 实现口令：按 TDD 规范执行 Task X.Y（先红留证 → 再绿留证 → 写台账 → 勾选 tasks）

