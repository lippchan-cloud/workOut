# Tasks: phase-6-share-export-demo

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `phase-6-share-export-demo`. Override with `/opsx:apply <other>`.

## 1. 事项 sheet 去掉身体列 (backend)

- [x] 1.1 **TDD** `CsvExportTest`：所有「事项列表」表头断言改为 `记录时间,类型,内容`；`exportRowsShouldAlignHeightToHistoryAtRecordedAt` 改为事项行不含 170/180，曲线 sheet 仍含 170 与 180。RED → `XlsxExportWriter.writeRecordsSheet` 三列 → GREEN。写入验证记录

## 2. 分享二级页与同级按钮 (frontend)

- [x] 2.1 **TDD** 日历「分享」「导出」同级（均含 `btn-ghost` 与 `btn-block`，导出不再 `btn-primary`）；点分享（已填身高体重）进入 `/calendar/share?...` 且主页不 POST `shareReports`。RED → 日历导航 + 去掉内联 POST → GREEN
- [x] 2.2 **TDD** `/calendar/share?date=` 会 POST `/api/v1/shareReports?date=`，展示返回 url 与复制；可返回日历。缺身高体重仍从日历拦截去 `/profile/body`。写入验证记录

## 3. 报告回首页与列表排版、缩放符号 (frontend)

- [x] 3.1 **TDD** `/report/:id` 有「回首页」链到 `/`。RED → ReportPage Link → GREEN
- [x] 3.2 **TDD** 报告/日历事项行时间与内容不挤在同一 flex 行（有 `record-list--stacked` 或 CSS column）。绿耗红食保持。RED → CSS + 结构 → GREEN
- [x] 3.3 **TDD** 曲线缩小/放大 `aria-label` 仍为缩小/放大，可见文本为 `−`/`+`。`ProfilePage.test` getByRole name 放大/缩小仍过。RED → GrowthCurve → GREEN

## 4. Demo 种子账号 (backend)

- [x] 4.1 **TDD** `DemoDataSeeder`（或生成器）在给定 clock 下产出记录落在今天±约 90 天、含 CONSUME/INTAKE 中英混合；`ApplicationRunner` `@Profile("!test")`，已存在 `demo` 则跳过；`saveAll` 批量，禁止 N+1。密码 `demo1234`。RED → seeder → GREEN。窄测不打满 SQLPub

## 5. 文档与 specs sync

- [x] 5.1 更新 `doc/workOut-产品文档.md`
- [x] 5.2 更新 `doc/workOut-功能文档.md`
- [x] 5.3 更新 `doc/workOut-技术架构.md`（CSV → xlsx、分享、种子）
- [x] 5.4 README 相关句（同级分享二级页、事项无身体列、demo 账号）
- [x] 5.5 按 openspec-sync-specs 把 delta 写入 `openspec/specs/`

## 6. 回归与收尾

- [x] 6.1 跑相关前后端测试并写入验证记录；全量 `mvn test` 若打满 SQLPub 则窄测并说明
- [x] 6.2 `openspec validate --change phase-6-share-export-demo`（若 CLI 支持）
- [x] 6.3 **不 commit、不 push**
