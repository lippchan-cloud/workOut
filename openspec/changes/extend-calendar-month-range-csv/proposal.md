## Why

用户回看训练/饮食记录时，目前只能在日历周条上点某一天查看并按日导出 CSV；无法按整月浏览，也无法一次跳到任意日期或自定义区间。产品文档已把「按周/按月批量导出」列为 P1，本次按用户明确需求提前纳入，补齐回看与带走闭环。

## What Changes

- 日历页在保留「按周选日」的前提下，增加 **按月** 与 **自定义日期区间** 两种筛选
- 按日模式增加 **日期选择器**，可直接跳到任意自然日，不必连点「上一周/下一周」
- 列表与「导出 CSV」均作用于 **当前筛选条件**（单日 / 整月 / 区间），仅当前用户数据
- 后端在现有 `GET /api/v1/dailyRecords` 与 `GET /api/v1/dailyRecords/exportCsv` 上扩展查询参数：保留 `date`（单日，不破坏既有契约），新增互斥的 `yearMonth` 与 `from`+`to`
- **不** 改记录创建页（消耗/摄入表单的 `recordedAt` 已可改）；**不** 做编辑/删除、图表、分页（区间上限约束即可）

## Capabilities

### New Capabilities

- （无 — 沿用既有 `calendar-view` / `csv-export` / `daily-record` 能力名，不另起能力）

### Modified Capabilities

- `calendar-view`: 日历页增加按月查看、任意跳日、自定义 from–to 区间；列表仍按 `recordedAt` 升序、消耗绿/摄入红；空态按筛选粒度提示
- `csv-export`: 导出 CSV 跟随当前筛选（单日文件名保持 `workout-YYYY-MM-DD.csv`；整月/区间使用对应文件名）；编码、列、BOM、仅本人、空表仅表头不变
- `daily-record`: 列表查询除 `date` 外支持 `yearMonth` 与 `from`+`to`（上海时区闭开区间）；参数互斥与非法区间返回 400；隔离规则不变

## Impact

- 后端：`DailyRecordController` / `DailyRecordService` 扩展 list 与 export 入参；仓储已有 `userId + recordedAt` 区间查询，无新表、无 N+1
- 前端：`CalendarPage` 增加筛选模式与控件；导出把当前筛选参数传给既有 `exportCsv`
- 测试：扩展 `DailyRecordQueryTest`、`CsvExportTest`、`CalendarPage.test.tsx`、`CsvExportClick.test.tsx`；红绿证据写入 `doc/workOut-TDD验证记录.md`
- 文档：相对 `doc/workOut-产品文档.md` §5.3，将「按月批量导出」从 P1 提前到本 change；不覆盖进行中的 `add-admin-cms-accounts`
- 不提交 git
