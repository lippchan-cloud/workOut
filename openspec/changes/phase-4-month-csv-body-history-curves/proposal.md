## Why

三期把日历列表做成「点进去看详情」，月视图与日列表仍经常只露出内容、看不到时分；CSV 只带事项本身，身高体重永远是「现在」或干脆没有；身体资料改了也没有历史，无法对照当时体态。用户需要把带走的表格和时间轴曲线做成可信的时间对齐，而不是一页堆网格。

## What Changes

- 日历月视图（及日/区间列表）每条事项展示 `recordedAt` 的本地日期+时分，不再只有内容。
- CSV 增加昵称、身高 cm、体重 kg 列；UTF-8 BOM 保持。有变更历史时，**按该条 `recordedAt` 匹配当时有效的身体档案**，禁止用当前资料覆盖所有天。
- 保存身体资料时写入变更历史（新表 `work_out_profile_history`：变更时间 + 字段快照）。注销账号时按 userId 批量删除历史。
- 日历增加二级页「变化曲线」（建议 `/calendar/trends`）：横轴时间，纵轴身高/体重（可切换或双系列），可附记录条数；无数据空态；返回回到日历。一级 Tab 仍是记录 / 日历 / 我的。
- 同步产品文档、功能文档、README 相关句与 OpenSpec specs。
- **不 BREAKING 路径**：`exportCsv` URL 不变；表头增列。不做组织/SSO/多环境/卡路里医疗建议。

## Capabilities

### New Capabilities

- `body-history`: 身体资料变更历史落库、按时间点解析当时快照、曲线页所需历史与条数序列（禁止 N+1）。

### Modified Capabilities

- `calendar-view`: 月/日/区间列表展示记录时间；日历二级入口进入变化曲线并返回日历。
- `daily-record`: CSV 增加身体资料列，并按 `recordedAt` 对齐历史快照。
- `user-profile`: 保存资料时写入变更历史（无变化可不新增行）。
- `ui-hierarchy`: 日历 Tab 下增加二级「变化曲线」，不堆在月网格同一屏。

## Impact

- 后端：Flyway 新表；`ProfileService` 写历史；`DailyRecordService.exportCsv` 一次加载区间历史再内存匹配；`GET /api/v1/profile/history`（或 trends）供曲线页；注销批量删历史。
- 前端：日历列表时间文案；`/calendar/trends` SVG 曲线；日历入口「变化曲线」。
- 文档：`doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README、本 change specs；实现后 sync 到 main specs。
- 测试：后端 MockMvc（历史、CSV 对齐、无 N+1 契约）；前端 Vitest（月列表时间、曲线路由与空态）。
