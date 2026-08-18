## Why

健身与控食用户需要低成本记下每日消耗/摄入，并按天回看与导出；多人共用设备时数据必须按账号隔离。仓库目前仅有产品/功能/技术文档，尚无应用代码。现在用 OpenSpec 冻结 MVP 行为契约，并以 TDD 驱动从零实现单体应用。

## What Changes

- 从零搭建 React + Spring Boot + MySQL 前后端不分离应用（CLI 一条命令启动）
- 新增用户注册/登录（JWT）；未登录可见导航壳，点 Tab 或业务操作跳转登录/注册
- 新增记录页：消耗 / 摄入各一条，日期默认可改；数据归属当前用户
- 新增日历页：按周展示、默认今日、正序列表、消耗绿/摄入红
- 新增按选中日导出 CSV（UTF-8 BOM，仅本人数据）
- 新增「我的」资料：昵称/身高/体重按用户隔离，支持退出登录
- **BREAKING**（相对早期「无登录单用户」设想）：全部业务接口必须鉴权；Profile/Record 按 `userId` 建模

## Capabilities

### New Capabilities

- `user-auth`: 注册、登录、JWT 发放与校验、401 处理、前端守卫与 redirect
- `app-shell`: 三 Tab 导航壳、默认落地记录壳、路由与未登录点击跳转
- `daily-record`: 新增消耗/摄入记录、字段校验、按用户持久化
- `calendar-view`: 周视图、选中日列表、颜色语义、空态文案
- `csv-export`: 按日导出 CSV（列定义、BOM、空表）
- `user-profile`: 按用户读写昵称/身高/体重、退出登录
- `app-bootstrap`: 工程脚手架、MySQL DDL、静态资源托管、CLI 启动

### Modified Capabilities

（无 — `openspec/specs/` 尚无既有能力；本次为绿场首次变更）

## Impact

- 新建后端：Spring Boot、Security/JWT Filter、Auth/DailyRecord/Profile API、JPA 或 MyBatis、JUnit 测试
- 新建前端：React Router、Auth 状态、三 Tab 页面、登录/注册页、API client
- 新建数据库表：`user`、`daily_record`、`profile`
- 文档对齐：`doc/` v1.1 与本 change 一致；实现阶段以本 change 的 specs/tasks 为准
- 依赖：MySQL 本地可用；JWT 密钥配置；无第三方 OAuth
