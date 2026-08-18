## Why

四期把身体历史、CSV 对齐和日历上的变化曲线做出来了，但仍不够用：曲线没有标准单位与时间轴、数据多时不能拖、放大只是视觉缩放；真正 CSV 分不出「事项 / 成长曲线」两个工作表；资料变更时间只能是服务器此刻；曲线入口还在日历而不是「我的」资料页；外发只有下载、没有可配置的 H5 报告链接。用户需要把带走（xlsx + 分享）和成长曲线做成可信、好看、且必须先填身高体重的完整路径。

## What Changes

- 成长曲线迁到「我的」身体资料三级页下方（表单含资料真实日期，下面就是曲线）；日历主路径不再放「变化曲线」。
- 曲线坐标轴有明确单位（身高 cm、体重 kg）与时间；数据量大时可横向拖动（pan）；放大/缩小改变时间粒度（hour/day/week/month），不是 CSS scale。
- 身体资料增加 `datetime-local`（默认此刻）；保存时把该时间写入 `profile_history.changedAt`（及当前 profile 的更新时间），不再只能服务器 now。
- 导出改为 **xlsx**（多工作表）：Sheet1 事项列表（含当时身体列），Sheet2 成长曲线点。入口文案可写「导出」；真正 CSV 无法分 sheet，故不坚持 csv。
- 日历增加「分享」；分享与导出均须先有身高和体重（前端拦截 + 后端拒绝），否则引导去资料页。
- 分享生成只读随机 token（URL 仍叫 id），公开报告页展示：用户名称、事项列表、成长曲线，并预留「建议分析」空态。访问地址用配置项 `WORKOUT_PUBLIC_BASE_URL` 拼接。
- 同步产品文档、功能文档、README 与 OpenSpec specs。
- **BREAKING（文件格式）**：`exportCsv` 实际返回 xlsx，不再是 UTF-8 BOM 文本 CSV。路径可保持以免前端路由大改，MIME/文件名改为 `.xlsx`。
- 不做组织/SSO/多环境拆分、真实医疗建议、commit。

## Capabilities

### New Capabilities

- `share-report`: 按日历筛选范围创建只读分享（随机 token）、公开 `GET /api/v1/reports/{id}`、H5 `/report/:id` 独立页（三块内容 + 建议分析占位）；创建时校验身高体重；base URL 可配置。

### Modified Capabilities

- `body-history`: `changedAt` 可用用户选择的资料真实日期；曲线数据仍一次查出；曲线交互为时间轴 + 单位 + pan + 粒度 zoom。
- `user-profile`: 身体资料页含真实日期控件；页下方展示成长曲线。
- `daily-record`: 导出改为 xlsx 双工作表；缺少身高或体重时拒绝导出。
- `calendar-view`: 增加分享按钮；移除日历主路径上的变化曲线入口。
- `ui-hierarchy`: 成长曲线属于「我的」身体资料三级页；公开报告页不走三 Tab 壳。

## Impact

- 后端：Apache POI 生成 xlsx；Flyway `work_out_share_report`；`ProfileRequest.changedAt`；导出/分享身高体重校验；公开报告 API；`WORKOUT_PUBLIC_BASE_URL`；注销批量删分享行。
- 前端：资料页日期 + 曲线（pan/zoom）；日历「导出」「分享」；`/report/:id` 独立页；拦截未填身高体重。
- 文档：`doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README、本 change specs；实现后 sync 到 main specs。
- 测试：后端窄测（xlsx sheet、changedAt、校验、分享公开读）；前端相关测（资料页曲线、导出拦截、报告页）。
