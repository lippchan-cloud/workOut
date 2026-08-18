## Context

Phase 2 已有：JWT 隔离、日历三日/月/区间、列表改删、补记、资料改密/注销、ADMIN CMS。前端仍是扁平页：`/profile` 一次渲染账号+身体数据；日历「上一周 / 下一周 / 补记」同级大按钮；列表上直接改删，没有详情 URL。

约束：不改记账规则；不加组织/SSO/多环境；周气泡禁止 N+1；TDD；Java 改动遵循 `java-architecture-master`；不 commit。仓库可能已有未提交的二期改动，本 change 只做增量。

## Goals / Non-Goals

**Goals:**

- 一级 Tab / 二级选择 / 三级表单或详情可走通、可返回
- 「我的」先三选项再进身体资料或账号安全
- 日历格子为主体：小周切换、hover/focus、当日条数气泡、列表进详情
- 详情 URL 刷新可打开（`GET /api/v1/dailyRecords/{id}`）
- 按钮层级与记录大按钮精致化，颜色仍绿耗红食
- 产品/功能文档与 OpenSpec 同步

**Non-Goals:**

- 新筛选接口、日历月网格大改、热量/动作库
- 组织、SSO、多环境
- 把详情做成只能靠列表 navigation state

## Decisions

### D1: 「我的」用独立路由，不用底部 sheet

**Choice:** `/profile` 二级选项；`/profile/body` 身体资料；`/profile/account` 账号安全（改密、注销；ADMIN 可见「后台管理」）。退出登录在二级选项层立即执行。返回用「返回」到 `/profile`。

**Alternatives:** 底部弹出层。独立路由对 Vitest、浏览器返回、直达更稳。

### D2: 日历选中日写入 `?date=`，详情独立路由

**Choice:** 日模式 URL `/calendar?date=YYYY-MM-DD`（缺省为今日）。详情 `/calendar/records/:id`，可带 `date` 便于返回。列表项点击进详情；编辑/删除只在详情页（复用二期 PUT/DELETE 与确认）。浏览器返回或「返回」回到 `/calendar?date=<该记录日>`。

**Alternatives:** 仅 `location.state`。刷新会丢，违反「URL 能打开」。

### D3: 周气泡一次 `from&to`，前端聚合

**Choice:** 日模式请求 `GET /api/v1/dailyRecords?from=<周一>&to=<周日>`（已有接口）。前端按上海/本地自然日聚合 count，过滤选中日列表。切周或跳转到另一周再请求一次。禁止按 7 天循环打 `?date=`。

**Alternatives:** 新 count 接口。现有区间查询已够用。

### D4: 详情 `GET /{id}`，跨用户 404

**Choice:** `GET /api/v1/dailyRecords/{id}` 复用 `requireOwned`（`id + userId + deleted=false`）。缺失、已删、他人记录一律 404「记录不存在」。不新增表。

**Alternatives:** 前端从周列表找 id。刷新详情会失败。

### D5: 按钮视觉 — 实色层级，去掉抢眼渐变

**Choice:** `.btn-primary` / `.btn-consume` / `.btn-intake` 用实色 token（`#16A34A` / `#DC2626` / 现有 accent），主 CTA 最小触控高度 44px；`.btn-ghost` 次要；新增 `.btn-text` / `.week-nav-btn` 小字控件。记录 `.btn-record-hero` 提高圆角、略收字号、保持大触控面。「补记」用次要/文字按钮，周切换用 `.week-nav-btn`。格子 `:hover` 与 `:focus-visible`。

**Alternatives:** 大改亮色主题。范围过大。

### D6: CMS 入口位置

**Choice:** 仅出现在 `/profile/account`，`isAdmin` 才渲染。不出现在登录页免登录入口（二期已禁）。

## Risks / Trade-offs

- [日模式从 `?date=` 改为 `?from&to`] → 更新既有日历测试断言，避免误报 RED
- [详情 404] → 中文提示「记录不存在」，提供回日历
- [Tab 高亮] → `/calendar/records/:id` 与 `/profile/*` 仍点亮对应一级 Tab（NavLink 默认前缀匹配）

## Migration Plan

无数据迁移。前端静态资源随下次 `build:static` / `start.sh` 发布。回滚即还原本 change 的前端路由与 `GET /{id}`。

## Open Questions

无。选项文案固定为：身体资料、账号安全、退出登录。
