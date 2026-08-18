## Why

一期/二期把记账、回看、资料能力做齐了，但界面仍是「一页摊开」：按钮同级偏大、「我的」一次露出身高体重改密注销、日历周切换抢过日期格子。用户需要清晰的一级 / 二级 / 三级路径，才能在不改业务规则的前提下把操作做轻、把主体做大。

## What Changes

- 明确信息架构：一级底部 Tab（记录 / 日历 / 我的）；二级为该 Tab 的选择层；三级为具体表单或详情。
- 「我的」进入后先出二级三选项（身体资料、账号安全、退出登录）；点选项再进三级页。改密/注销放在账号安全内，不与身高体重同页。ADMIN 的 CMS 入口仍仅 ADMIN 可见，不打回免登录。
- 日历：上一周 / 下一周改为小控件；日期格子为视觉主体，支持 hover/focus；有记录的格子右上角显示当天条数气泡。点某天仍可在下方看列表；点某一条必须进入只读事项详情页，再提供编辑/删除（复用二期 PUT/DELETE）。「补记」保留为二级动作，不得做成和周切换一样大的按钮。
- 事项详情可直达 URL（建议 `/calendar/records/:id`）；浏览器返回回到日历选中日。刷新详情 URL 必须能打开，不能只靠列表 state。
- 重做按钮层级：主 CTA / 次要 / 文字小按钮；对齐消耗绿 / 摄入红。记录大按钮保留但更精致（圆角、字号、触控高度）。不要花哨渐变大改。
- 后端：周气泡用已有 `GET /api/v1/dailyRecords?from&to` 一次查出该周再前端聚合，禁止 N+1。详情补 `GET /api/v1/dailyRecords/{id}`（JWT 隔离，跨用户/缺失 404）。
- 同步产品文档、功能文档、OpenSpec specs；README 若仍写「我的一页填完所有」则改一句。不做组织/SSO/多环境，不改记账业务规则。

## Capabilities

### New Capabilities

- `ui-hierarchy`: 一级/二级/三级信息架构、按钮视觉层级、记录大按钮精致化。

### Modified Capabilities

- `user-profile`: 「我的」先二级选项层，再进身体资料 / 账号安全三级页；退出登录在选项层。
- `calendar-view`: 小周切换、格子 hover/focus、数量气泡、列表进详情、补记为二级动作。
- `daily-record`: 新增按 id 查询（JWT 隔离、404）；周视图继续用区间列表一次查询。

## Impact

- 前端：`App.tsx` 路由、`ProfilePage` 拆菜单/身体/账号、`CalendarPage` 周控件与气泡、新增记录详情页、`index.css` 按钮与格子样式。
- 后端：`DailyRecordController` / `DailyRecordService` 增加 `GET /{id}`；无新表、无新筛选参数。
- 文档：`doc/workOut-产品文档.md`、`doc/workOut-功能文档.md`、README、本 change 的 specs；实现后 sync 到 main specs。
- 测试：前端 Vitest（我的三级、日历 hover/气泡、详情路由、小周切换）；后端 MockMvc（GET by id 隔离与 404）。
