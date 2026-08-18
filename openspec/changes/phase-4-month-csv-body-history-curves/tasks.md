# Tasks: phase-4-month-csv-body-history-curves

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `phase-4-month-csv-body-history-curves`. Override with `/opsx:apply <other>`.

## 1. Profile history table and trends API (backend)

- [ ] 1.1 **TDD** `ProfileHistoryTrendsTest`：`PUT /api/v1/profile` 首次保存后 `GET /api/v1/profile/trends` 有一条 `bodyHistory`；改身高再 PUT 增为两条且升序；相同值再 PUT 仍一条。无 Token 401。RED → Flyway V4 `work_out_profile_history` + upsert 写快照 + trends → GREEN。写入验证记录
- [ ] 1.2 **TDD** 同一测试类：用户 A 有历史与 2026-08-18 两条记录时 `recordCounts` 含该日 count=2；用户 B 看不到 A。RED → 一次 GROUP BY 聚合 → GREEN

## 2. CSV 按 recordedAt 对齐身体信息 (backend)

- [ ] 2.1 **TDD** `CsvExportTest`：空日导出表头为 `记录时间,类型,内容,昵称,身高cm,体重kg` 且仍有 UTF-8 BOM。RED → 改表头 → GREEN（同步旧断言）
- [ ] 2.2 **TDD** 先保存身高 170 并记 `早训`，再保存 180 并记 `晚训`；导出区间内 `早训` 行身高 170、`晚训` 行 180。RED → exportCsv 一次加载历史内存匹配 → GREEN

## 3. 注销删除历史

- [ ] 3.1 注销账号时 `deleteByUserId` 批量删除 `work_out_profile_history`（禁止循环）。可并入既有注销测试或补一条 trends 为空的断言

## 4. 日历列表时间与曲线页 (frontend)

- [ ] 4.1 **TDD** `week.test.ts`：增加上海时区时分格式化（日 `HH:mm`，月 `MM-DD HH:mm`）。RED → `week.ts` → GREEN
- [ ] 4.2 **TDD** `CalendarPage.test.tsx`：日列表与月列表均出现 `07:30`（月列表可带日期）。RED → 列表展示时间 → GREEN
- [ ] 4.3 **TDD** 日历有「变化曲线」进 `/calendar/trends`；曲线页返回 `/calendar`；trends 全空时中文空态、无折线。RED → 路由 + TrendsPage SVG → GREEN

## 5. 文档与 specs sync

- [ ] 5.1 更新 `doc/workOut-产品文档.md`（CSV 身体列、资料历史、变化曲线）
- [ ] 5.2 更新 `doc/workOut-功能文档.md` 页面/字段/接口
- [ ] 5.3 README 相关句（列表带时间、CSV 带身体资料、曲线）
- [ ] 5.4 按 openspec-sync-specs 把 delta 写入 `openspec/specs/`

## 6. 回归与收尾

- [ ] 6.1 跑相关前后端测试并写入验证记录；全量 `mvn test` 若打满 SQLPub 则窄测并说明
- [ ] 6.2 `openspec validate --change phase-4-month-csv-body-history-curves`（若 CLI 支持）
- [ ] 6.3 **不 commit、不 push**
