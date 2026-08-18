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

前后端不分离：先把前端构建产物复制到 Spring 静态目录，再启动后端（默认端口 `8080`）。

```bash
# 方式 A：一键脚本
./scripts/start.sh

# 方式 B：分步
cd frontend && npm install && npm run build:static
cd ../backend && mvn spring-boot:run
```

环境变量（勿把真实密码写入已提交的 `application.yml`）：

| 变量 | 说明 |
| --- | --- |
| `WORKOUT_DB_URL` | JDBC URL，默认 `jdbc:mysql://localhost:3306/workout?...` |
| `WORKOUT_DB_USER` | 数据库用户，默认 `root` |
| `WORKOUT_DB_PASSWORD` | 数据库密码 |
| `WORKOUT_JWT_SECRET` | JWT HMAC 密钥（生产必须覆盖） |

## 测试命令

```bash
cd backend && mvn test
cd frontend && npm test
```

窄测示例：`cd backend && mvn -q test -Dtest=AuthLoginTest`

## 文档
- [产品文档](doc/workOut-产品文档.md)
- [功能文档](doc/workOut-功能文档.md)
- [技术架构](doc/workOut-技术架构.md)
- 数据库连接说明见 [doc/workOut-数据库连接.md](./doc/workOut-数据库连接.md)（含本地密钥，**不要**复制进已提交配置）
- [验收记录](doc/验收记录.md)（T00–T13）

## OpenSpec + TDD
- 变更：[`openspec/changes/init-workout-mvp`](openspec/changes/init-workout-mvp/proposal.md)
- 任务清单：[`tasks.md`](openspec/changes/init-workout-mvp/tasks.md)
- **TDD 规范（门禁）：** [`doc/workOut-TDD规范.md`](doc/workOut-TDD规范.md)
- **TDD 验证台账（红绿证据）：** [`doc/workOut-TDD验证记录.md`](doc/workOut-TDD验证记录.md)
- 实现计划索引：[`docs/superpowers/plans/2026-08-18-init-workout-mvp.md`](docs/superpowers/plans/2026-08-18-init-workout-mvp.md)
- 校验：`openspec validate init-workout-mvp`
- 实现口令：按 TDD 规范执行 Task X.Y（先红留证 → 再绿留证 → 写台账 → 勾选 tasks）

