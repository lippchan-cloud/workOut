## 1. Backend period list (TDD)

- [x] 1.1 写失败测试扩展 `backend/src/test/java/com/workout/record/DailyRecordQueryTest.java`：`yearMonth=2026-08` 只返回该月记录且 `data.from`/`data.to` 正确；`from`+`to` 闭区间不含区间外记录；`date` 与同日 `from=to` 条数一致。跑 RED，写入 `doc/workOut-TDD验证记录.md`
- [x] 1.2 最小实现 `DailyRecordService`/`DailyRecordController` 解析 `yearMonth` 与 `from`/`to`（上海时区，一次仓储区间查询）。同一命令 GREEN，补验证记录后勾选
- [x] 1.3 写失败测试：混用 `date`+`yearMonth`、`from > to`、跨度 > 366 天均 HTTP 400 中文 `msg`；仅 `date` 仍 200。RED → 校验逻辑 → GREEN
- [x] 1.4 写失败测试：用户 B 查 `yearMonth` 看不到用户 A 的月记录。可放在 `DailyRecordQueryTest` 或 `DailyRecordIsolationTest`。RED → 确认查询始终带 JWT userId → GREEN

## 2. Backend period CSV (TDD)

- [x] 2.1 写失败测试扩展 `backend/src/test/java/com/workout/record/CsvExportTest.java`：`yearMonth=2026-08` 文件名 `workout-2026-08.csv`、BOM、中文类型、仅该月本人行；空月仅表头。RED，写入验证记录
- [x] 2.2 同一测试类：`from`+`to` 跨日文件名 `workout-YYYY-MM-DD_YYYY-MM-DD.csv`；同日区间文件名 `workout-YYYY-MM-DD.csv`。RED → 扩展 `exportCsv` 复用 period 解析 → GREEN
- [x] 2.3 写失败测试：`exportCsv` 混用参数 400；`yearMonth` 无 Token 仍 401。RED → GREEN

## 3. Frontend calendar modes (TDD)

- [x] 3.1 写失败测试扩展 `frontend/src/CalendarPage.test.tsx`：可切换「按月」；出现月份控件；空月文案「这个月还没有记录」；有数据时消耗绿/摄入红。RED → `CalendarPage` 按月模式 → GREEN
- [x] 3.2 写失败测试：按日模式有日期跳转控件；选择今天以外的日期后请求 `date=YYYY-MM-DD` 且周条包含该日。RED → 跳转控件 → GREEN
- [x] 3.3 写失败测试：「自定义」模式两个日期控件；设置 from/to 后请求带 `from` 与 `to`；空区间文案「这段时间还没有记录」。RED → 区间模式 → GREEN

## 4. Frontend CSV follows filter (TDD)

- [x] 4.1 写失败测试扩展 `frontend/src/CsvExportClick.test.tsx`（或 Calendar 测）：按月模式下点「导出 CSV」请求 URL 含 `yearMonth=` 且不含 `date=`。RED → 导出参数与当前模式一致 → GREEN
- [x] 4.2 未登录点导出仍跳 `/login?redirect=/calendar`（回归）。RED（若被破坏）→ GREEN

## 5. 门禁收尾

- [x] 5.1 跑相关回归：`DailyRecordQueryTest`、`DailyRecordIsolationTest`、`CsvExportTest`、前端 `CalendarPage.test.tsx`、`CsvExportClick.test.tsx`；命令与结果写入验证记录
- [x] 5.2 `openspec validate extend-calendar-month-range-csv` 通过
- [x] 5.3 不提交 git、不纳入 `backend/target/**`；不改 `add-admin-cms-accounts`
