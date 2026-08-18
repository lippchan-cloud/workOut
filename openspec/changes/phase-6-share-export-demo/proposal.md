## Why

五期把分享与 xlsx 带走做出来了，但日历上分享是 ghost、导出是 primary block，不对等；分享链接内联贴在日历主页；事项工作表仍带昵称/身高/体重，和「身体只应出现在成长曲线」冲突；报告页不能回首页；缩放按钮文案不够好看；技术架构文档仍写 CSV；本地缺少一份覆盖上下约三个月的丰富测试账号。六期要把带走路径做对称、表格职责分清，并补齐可演示数据。

## What Changes

- 日历「分享」与「导出」同级：均为 `btn btn-ghost btn-block`（或等价 action-pair），不再一主一次。
- 点「分享」进入二级页 `/calendar/share?...`（带上当前筛选 query：`date` / `yearMonth` / `from&to`）；该页再 POST `/api/v1/shareReports`，展示链接与复制，可返回日历。缺身高体重仍拦截去 `/profile/body`。日历主页不再内联贴分享链接。
- 导出工作表「事项列表」表头改为 `记录时间,类型,内容`，去掉昵称/身高/体重列；身体数据只留在「成长曲线」sheet。
- 公开报告页提供「回首页」链到 `/`。
- 成长曲线缩放按钮可见 `−` / `+`，`aria-label` 仍为缩小/放大。
- 事项/消耗列表：时间与内容分行或左时间右内容且有足够间距，不要挤在一行；绿耗红食保持。报告页、日历列表、记录首页消耗列表一并修。
- 非 test profile 启动时种子账号 `demo` / `demo1234`（若已存在则跳过）；一次 batch 写入约过去 3 个月 + 未来 3 个月的中英混合消耗/摄入与随月变化的身体历史。禁止 N+1。
- 同步产品文档、功能文档、**技术架构**（五期漏改，仍写 CSV）、README、OpenSpec specs。
- 不做组织/SSO/多环境拆分、commit。

## Capabilities

### New Capabilities

- （无）种子账号不单独成能力，归入现有启动/认证说明。

### Modified Capabilities

- `calendar-view`: 分享与导出同级；分享走 `/calendar/share` 二级页；日历列表时间与内容不挤。
- `share-report`: 报告页可回首页 `/`；事项列表排版不挤。
- `daily-record`: 事项 sheet 不含身体列；曲线 sheet 仍含身高体重。
- `ui-hierarchy`: 分享为日历二级；报告页有回首页。
- `user-profile`: 曲线缩放控件可见 `−`/`+`，无障碍名仍为缩小/放大。
- `body-history`: 缩放控件可见 `−`/`+`，行为仍为时间粒度而非 CSS scale。

## Impact

- 前端：日历 action-pair、`SharePage`、报告回首页、GrowthCurve −/+、`.record-list` CSS。
- 后端：`XlsxExportWriter.writeRecordsSheet` 三列；`DemoDataSeeder` ApplicationRunner（`@Profile("!test")`）；测试改断言。
- 文档：产品、功能、技术架构、README、本 change specs；实现后 sync 到 main specs。
- 测试：前端相关 Vitest；后端窄测 `CsvExportTest` + 种子相关；不打满全量 `mvn test`。
