# Tasks: phase-5-share-report-curve-xlsx

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `phase-5-share-report-curve-xlsx`. Override with `/opsx:apply <other>`.

## 1. 资料真实日期写入 changedAt (backend)

- [x] 1.1 **TDD** `ProfileHistoryTrendsTest`：`PUT /api/v1/profile` 带 `changedAt` `2026-08-01T08:00:00+08:00` 后 trends 该条 `changedAt` 为同一瞬间。RED → ProfileRequest + upsert 用客户端时间 → GREEN。写入验证记录

## 2. 导出 xlsx 与身高体重闸门 (backend)

- [x] 2.1 **TDD** 缺身高或体重时 `GET /api/v1/dailyRecords/exportCsv` 返回 400「请先填写身高和体重」。RED → 导出前校验当前 profile → GREEN
- [x] 2.2 **TDD** 已填身高体重时导出为 xlsx：两个 sheet「事项列表」「成长曲线」；事项列表表头与 recordedAt 对齐仍成立；文件名 `.xlsx`。RED → POI + 改 MIME → GREEN（同步旧 CSV 断言）

## 3. 分享与公开报告 (backend)

- [x] 3.1 脚手架 Flyway V5 `work_out_share_report`；`FlywayMigrationTest` 含该表。标注非行为 TDD
- [x] 3.2 **TDD** 已填身高体重时 `POST /api/v1/shareReports?date=...` 返回非递增 token 与 `{base}/report/{id}`；缺身高体重 400；无 JWT 401。RED → 表 + 创建服务 + `WORKOUT_PUBLIC_BASE_URL` → GREEN
- [x] 3.3 **TDD** 无登录 `GET /api/v1/reports/{id}` 返回快照（displayName、records、from/to、bodyHistory、advice 空）；未知 id 404。RED → permitAll + snapshot → GREEN
- [x] 3.4 注销批量 `deleteByUserId` 分享行；SPA fallback `/report/**`

## 4. 曲线迁到资料页 + pan/zoom (frontend)

- [x] 4.1 **TDD** `/profile/body` 有资料真实日期（默认今日）且 PUT 带 `changedAt`；表单下方有成长曲线；空态中文。RED → ProfileBodyPage + 迁 Trends 组件 → GREEN
- [x] 4.2 **TDD** 曲线身高系列露出 `cm`、时间轴；点「放大」精度变细（如 day→hour），不是 CSS scale。RED → 时间窗口 + precision 状态 → GREEN
- [x] 4.3 **TDD** 日历主路径无「变化曲线」；`/calendar/trends` 转到 `/profile/body`

## 5. 导出/分享闸门与报告页 (frontend)

- [x] 5.1 **TDD** 日历按钮「导出」「分享」；缺身高体重不发起下载/POST 并去 `/profile/body`；有资料则可导出 xlsx 文件名
- [x] 5.2 **TDD** `/report/:id` 无三 Tab；上下为用户名称、事项列表、成长曲线、建议分析空态；范围可见

## 6. 文档与 specs sync

- [x] 6.1 更新 `doc/workOut-产品文档.md`
- [x] 6.2 更新 `doc/workOut-功能文档.md`
- [x] 6.3 README 相关句
- [x] 6.4 按 openspec-sync-specs 把 delta 写入 `openspec/specs/`

## 7. 回归与收尾

- [x] 7.1 跑相关前后端测试并写入验证记录；全量 `mvn test` 若打满 SQLPub 则窄测并说明
- [x] 7.2 `openspec validate --change phase-5-share-report-curve-xlsx`（若 CLI 支持）
- [x] 7.3 **不 commit、不 push**
