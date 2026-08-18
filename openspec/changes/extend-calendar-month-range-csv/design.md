## Context

日历页 `/calendar` 已能按周条选中单日、拉取 `GET /api/v1/dailyRecords?date=`、导出 `GET /api/v1/dailyRecords/exportCsv?date=`（UTF-8 BOM、仅本人）。仓储已有 `userId + recordedAt [start, end)` 一次查询，无新表需求。缺口是：没有整月查询/列表、没有任意跳日、没有 from–to 区间；CSV 只能跟单日走。

约束：JWT `userId` 隔离；Asia/Shanghai；周仍从周一起；沿用 `modules.record` 分层与 `{ code, msg, data }`；TDD 红绿留证；java-architecture-master（public 方法 `log.info`、禁止 N+1）；不改进行中的 `add-admin-cms-accounts`。

## Goals / Non-Goals

**Goals:**
- 同一对 list / exportCsv 接口支持三种互斥筛选：单日 `date`、整月 `yearMonth`、闭区间 `from`+`to`
- 日历页三种模式：按日（保留周条 + 日期跳转）、按月、自定义区间；列表与导出使用同一组参数
- 单日契约不变：既有 `DailyRecordQueryTest` / `CsvExportTest` / 按日文件名仍成立
- 空区间仍导出仅表头的 CSV

**Non-Goals:**
- 记录创建页（`RecordPage` 的 `datetime-local` 已可改时间）
- 记录编辑/删除、图表、分页、按周批量导出按钮（周模式仍按「选中日」导出）
- 新表 / Flyway / 新 Maven 依赖
- 修正既有 CSV 换行 `\n` 与功能文档 `\r\n` 的历史差异（本 change 不碰）

## Decisions

### D1: 扩展现有 GET 参数，而不是新路径或废弃 `date`

**Choice:** `GET /api/v1/dailyRecords` 与 `GET /api/v1/dailyRecords/exportCsv` 共用同一套互斥查询参数：

| 模式 | 参数 | 上海时区区间 |
| --- | --- | --- |
| 单日 | `date=YYYY-MM-DD` | `[date 00:00, date+1 00:00)` |
| 整月 | `yearMonth=YYYY-MM` | `[该月 1 日 00:00, 次月 1 日 00:00)` |
| 区间 | `from` + `to`（均含当天） | `[from 00:00, to+1 00:00)` |

恰好一种模式；混用、缺一半区间、`from > to`、区间跨度 > 366 天 → HTTP 400 + 中文 `msg`。无 Token 仍 401。

**Alternatives:**
1. **只保留 `from`/`to`，废弃 `date`（不推荐）**：后端更简单，但 **BREAKING**，既有测试与前端全部改写。
2. **新路径 `/dailyRecords/period`（不推荐）**：多一套鉴权/信封/导出分叉，和现有按日能力重复。
3. **本方案（推荐）**：`date` 继续当便捷别名；月/区间是加法。

**Rationale:** 仓储已经按 Instant 区间查；缺口在参数解析与 UI，不在存储。

### D2: 列表响应对日模式保持 `data.date`，月/区间带 `from`/`to`

**Choice:**
- 单日：继续 `data.date` + `data.list`（可额外带 `from`/`to` 等于该日，前端仍读 `list`）
- 整月：`data.yearMonth` + `data.from` + `data.to` + `data.list`（`to` 为该月最后一天）
- 区间：`data.from` + `data.to` + `data.list`

排序仍 `recordedAt` 升序、再 `id` 升序。一次仓储调用，禁止按日循环查库。

**Alternatives:** 统一只返回 `from`/`to`（会改掉现有 `$.data.date` 断言）；按日分页（个人账本过重）。

### D3: CSV 跟当前筛选走，列定义不变

**Choice:** `exportCsv` 先解析与 list 相同的区间，再生成现有三列（`记录时间,类型,内容`）、UTF-8 BOM、类型中文。文件名：

- 单日：`workout-YYYY-MM-DD.csv`（已有）
- 整月：`workout-YYYY-MM.csv`
- 区间且起止不同日：`workout-YYYY-MM-DD_YYYY-MM-DD.csv`
- 区间且起止同一天：与单日相同 `workout-YYYY-MM-DD.csv`

空结果：仅表头。鉴权失败：401，SPA 跳 `/login?redirect=/calendar`。

**Alternatives:** 前端用当前 list JSON 拼 CSV（Excel/BOM/类型文案易漂）；单独导出服务（过重）。

### D4: 日历页三种模式，按月用扁平时间序列而非月网格

**Choice:** `CalendarPage` 增加筛选切换「按日 / 按月 / 自定义」：

- **按日（默认）**：保留周条与上一周/下一周；增加 `type="date"`「跳转到」，选中后定位该周并选中该日；列表/导出仍用 `date=`
- **按月**：`type="month"`，默认当前月；一次列出该月全部记录；导出 `yearMonth=`
- **自定义**：`from`/`to` 两个 `type="date"`，默认均为今天；导出 `from`&`to`

颜色语义不变。空态：「这一天还没有记录」/「这个月还没有记录」/「这段时间还没有记录」。

**Alternatives:** 月历格子点选某日（那是按日的另一种 UI，不解决「整月带走」）；只有区间控件、用预设代替按月（少一个显式「按月」入口，不贴用户原话）。

### D5: 后端落点与 TDD

**Choice:** 仍在 `com.workout.modules.record`：

```
api          DailyRecordController（list / exportCsv 解析互斥参数）
application  DailyRecordService（抽出 resolvePeriod → listByPeriod / exportCsv）
infrastructure DailyRecordRepository（已有区间方法，不新增）
```

无 DDL。新增/改动的 public 方法：入口 `log.info` 打 userId 与筛选键（date/yearMonth/from/to），结束打 size/bytes；禁止打印 token。

TDD 顺序：
1. 先扩后端 `DailyRecordQueryTest` / `CsvExportTest`（月、区间、互斥 400、隔离、366 上限）→ RED → 最小实现 → GREEN
2. 再扩前端 `CalendarPage.test.tsx` / `CsvExportClick.test.tsx`（模式切换、跳日、月列表、导出 query）
3. 每步写入 `doc/workOut-TDD验证记录.md` 后再勾 tasks
4. 回归既有按日查询/导出/隔离

**Package / table / JWT:** 不新增包层、不改表、不改 JWT；筛选永不信任客户端 `userId`。

## Risks / Trade-offs

- [Risk] 整月/366 天一次返回可能较大 → Mitigation：个人账本可接受；硬顶 366 天；后续若慢再加分页（禁止先全表再内存切）
- [Risk] 参数互斥让客户端易传错 → Mitigation：400 中文消息；前端三种模式只发对应参数
- [Risk] 改 Controller 签名破坏按日测试 → Mitigation：`date` 仍为合法单日模式；先跑旧测再加新测
- [Trade-off] 按月是扁平列表不是月网格 → 更贴「按月查看+整月导出」；任意某日用「跳转到」解决

## Migration Plan

无 DDL。部署后旧客户端只传 `date` 仍可用。回滚：恢复仅 `date` 的 Controller 与日历 UI。

## Open Questions

无。默认按日；按月为当月全量列表；自定义为闭区间；CSV 与列表同一筛选。
