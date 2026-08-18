# workOut
记录每日训练内容（按账号隔离）

## 核心功能：
1. 登录 / 注册：用户名 + 密码，JWT；未登录可见导航壳，点 Tab 或业务操作跳转登录。
2. 导航栏第一个，用户可以记录，当日消耗项和当日摄入项。
3. 当日消耗项，是一个输入框，可以记录一条内容，记录日期默认是操作时间，也可以自己更换
4. 当日摄入项，是一个输入框，可以记录一条内容，记录日期默认是操作时间，也可以自己更换
5. 导航栏，第二个，是一个日历，按周显示，默认选中今日
6. 选中日期后，下面有列表，按照时间正序展示（含时分，时间与内容分行），消耗绿色，摄入红色。「分享」与「导出」同级；点分享进入 `/calendar/share` 二级页生成 H5，导出为 xlsx（事项列表无身体列，成长曲线 sheet 有身高体重）。须先填身高和体重。
7. 我的：先三个选项（身体资料 / 账号安全 / 退出登录）；身体资料含真实日期，页下方是成长曲线（单位 cm/kg，可拖、−/+ 改时间粒度）；改资料会留历史；支持退出登录。公开报告页 `/report/:id` 不走三 Tab，可回首页。

本地演示账号（非 test 环境首次启动自动写入，已存在则跳过）：用户名 `demo`，密码 `demo1234`，约过去 3 个月 + 未来 3 个月的中英混合消耗/摄入与身体历史。

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

前后端不分离：启动或镜像构建时再生成前端静态资源并复制到 Spring 静态目录，**不入库**；再启动后端（默认端口 `8080`）。

```bash
# 方式 A：一键脚本
./scripts/start.sh

# 方式 B：分步
cd frontend && npm install && npm run build:static
cd ../backend && mvn spring-boot:run
```

### Docker（推荐给「只想打开浏览器用」）

单镜像连**项目同一套 MySQL**（默认 SQLPub `inv_doc`，与 `application.yml` 一致），**不用**容器内嵌 H2。需本机/容器网络能访问该库。

```bash
docker build -t workout:local .
docker run --rm -p 8080:8080 workout:local
# 浏览器打开 http://localhost:8080
# 若本机 8080 被占用：-p 18080:8080 → http://localhost:18080
```

可选 Compose：

```bash
docker compose up --build
```

镜像默认 `SPRING_PROFILES_ACTIVE=docker`，datasource 与本地开发相同。可选环境变量：`WORKOUT_DB_*`、`WORKOUT_JWT_SECRET`。

若改连**本机 MySQL**（而非 SQLPub），容器内主机名请用 `host.docker.internal`（勿用 `localhost`，那会指向容器自身），例如：

```bash
docker run --rm -p 18080:8080 \
  -e WORKOUT_DB_URL='jdbc:mysql://host.docker.internal:3306/inv_doc?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai' \
  -e WORKOUT_DB_USER=... \
  -e WORKOUT_DB_PASSWORD=... \
  workout:local
```

数据库连接与 JWT 默认值已写入 `backend/src/main/resources/application.yml`（私有仓）。不设环境变量即可启动；下列变量仅为可选覆盖：

| 变量 | 说明 |
| --- | --- |
| `WORKOUT_DB_URL` | 可选，覆盖 JDBC URL（含 docker profile） |
| `WORKOUT_DB_USER` | 可选，覆盖数据库用户 |
| `WORKOUT_DB_PASSWORD` | 可选，覆盖数据库密码 |
| `WORKOUT_JWT_SECRET` | 可选，覆盖 JWT HMAC 密钥 |
| `WORKOUT_PUBLIC_BASE_URL` | 可选，分享 H5 链接基址，默认 `http://localhost:8080` |

## 测试命令

后端集成测试直连 SQLPub MySQL（见 `backend/src/test/resources/application-test.yml`，与主配置同一实例），**不再使用 H2**。测试会向 workout 相关表写入数据；用户名采用 UUID 后缀隔离。需本机可访问 `mysql5.sqlpub.com:3310`。

```bash
cd backend && mvn test
cd frontend && npm test
```

窄测示例：`cd backend && mvn -q test -Dtest=AuthLoginTest`

## 后台 CMS（仅管理员）

独立入口：`http://localhost:8080/cms`（账号安全页「后台管理」）。须 **ADMIN JWT**；未登录跳转 `/login?redirect=/cms`；普通用户 403 / 页面拒绝。顶栏功能栏：**概览** `/cms`、**账户列表** `/cms/accounts`、**用户详情** `/cms/users/:userId`、**报告** `/cms/reports`。详情与报告只打开用户已有分享，不代生成。JSON 不含密码哈希。

## 文档
- [产品文档](doc/workOut-产品文档.md)
- [功能文档](doc/workOut-功能文档.md)
- [技术架构](doc/workOut-技术架构.md)
- 数据库连接说明见 [doc/workOut-数据库连接.md](./doc/workOut-数据库连接.md)（私有仓，默认已写入 `application.yml`）
- [验收记录](doc/验收记录.md)（T00–T13）

## OpenSpec + TDD
- 变更：[`openspec/changes/init-workout-mvp`](openspec/changes/init-workout-mvp/proposal.md)、[`openspec/changes/add-admin-cms-accounts`](openspec/changes/add-admin-cms-accounts/proposal.md)
- 任务清单：[`init-workout-mvp/tasks.md`](openspec/changes/init-workout-mvp/tasks.md)、[`add-admin-cms-accounts/tasks.md`](openspec/changes/add-admin-cms-accounts/tasks.md)
- **TDD 规范（门禁）：** [`doc/workOut-TDD规范.md`](doc/workOut-TDD规范.md)
- **TDD 验证台账（红绿证据）：** [`doc/workOut-TDD验证记录.md`](doc/workOut-TDD验证记录.md)
- 实现计划索引：[`docs/superpowers/plans/2026-08-18-init-workout-mvp.md`](docs/superpowers/plans/2026-08-18-init-workout-mvp.md)
- 校验：`openspec validate init-workout-mvp`
- 实现口令：按 TDD 规范执行 Task X.Y（先红留证 → 再绿留证 → 写台账 → 勾选 tasks）

