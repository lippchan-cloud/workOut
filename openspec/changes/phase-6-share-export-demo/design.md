## Context

五期已有：日历「导出」（xlsx 双 sheet，事项含身体列）、「分享」（主页内联 POST 后贴链接）、公开 `/report/:id`、资料页成长曲线（放大/缩小文字按钮）。增量做在未提交的二～五期之上，不还原。约束：TDD；Java `java-architecture-master`（public 方法 `log.info`、禁止 N+1）；时区 Asia/Shanghai。

## Goals / Non-Goals

**Goals:**

- 分享与导出按钮同级
- 分享走日历二级页 `/calendar/share?...`，主页不再内联贴链接
- 事项 sheet 仅 `记录时间,类型,内容`；身体只在成长曲线 sheet
- 报告页可回首页 `/`
- 曲线缩放可见 `−`/`+`，aria 仍为缩小/放大
- 列表时间与内容不挤（报告、日历、同类列表）
- `demo`/`demo1234` 种子（`@Profile("!test")`），覆盖约过去 3 个月 + 未来 3 个月
- 同步产品/功能/**技术架构**/README/specs

**Non-Goals:**

- 组织 / SSO / 多环境
- commit / push
- 改变分享 API 或 token 规则
- 全量 `mvn test` 打满 SQLPub

## Decisions

### D1: 分享与导出同级 ghost block

**Choice:** 日历主路径两个按钮均为 `btn btn-ghost btn-block`，包在 `action-pair`（`display:grid; grid-template-columns:1fr 1fr; gap`）里，视觉等权。去掉导出的 `btn-primary`。

**Alternatives:** 两个都 primary（过重）；保持一主一次（用户明确反对）。

### D2: 分享二级页 `/calendar/share`，创建动作在二级页

**Choice:** 日历点「分享」**不** POST。先 `requireBodyOrRedirect`，通过后 `navigate(/calendar/share?...)`：

- 按日：`date=YYYY-MM-DD`
- 按月：`yearMonth=YYYY-MM`
- 自定义：`from=` & `to=`

`SharePage` 在 AppShell 内（日历 Tab 仍高亮）。进入后 POST `/api/v1/shareReports` 带同一 query；展示 `url`、复制按钮（`navigator.clipboard`，失败则选中文本）、「返回日历」回到带筛选的日历。主页去掉 `shareUrl` 内联。

缺身高体重：仍拦截去 `/profile/body`，不进入二级页。

SPA fallback 已有 `/calendar/**`，无需新映射。

**Alternatives:** 主页 POST 后跳转并带 token（仍会在主页闪一下）；独立 `/share` 脱离日历层级。

### D3: 事项 sheet 去掉身体列

**Choice:** `XlsxExportWriter.writeRecordsSheet` 表头 `记录时间,类型,内容`。不再调用 `ProfileHistoryResolver` 填事项行。`write()` 仍接收 history 仅供曲线 sheet。`CsvExportTest` 所有「事项列表」含身高的断言改为三列表头；`exportRowsShouldAlignHeightToHistoryAtRecordedAt` 改为：事项行**不含** 170/180，曲线 sheet **含** 170 与 180。

**Alternatives:** 事项 sheet 保留昵称不要身高（用户说去掉昵称/身高/体重）。

### D4: 报告页回首页

**Choice:** `ReportPage` 顶部「回首页」`<a href="/">` 或 `Link to="/"`。公开页无 JWT 也可进记录壳。

### D5: 缩放按钮 − / +

**Choice:** 按钮可见文本 `−` 与 `+`（U+2212 minus），`aria-label` 仍为「缩小」「放大」。`getByRole({ name: "放大" })` 继续通过。样式用紧凑 ghost 圆形/方形，避免两个大 pill 并排。

### D6: 列表时间与内容分行

**Choice:** `.record-list__open` 与报告页非按钮行均 `flex-direction: column; align-items: stretch; gap: 0.35rem`。时间一行、内容下一行；内容可换行。绿耗红食 class/inline color 不变。日历可点行与报告只读行共用同一套 class。

**Alternatives:** 左时间右内容（窄屏仍会挤，故选分行）。

### D7: Demo 种子 ApplicationRunner

**Choice:** `DemoDataSeeder implements ApplicationRunner`，`@Profile("!test")`，`@Component`。

- 用户名 `demo`，密码 `demo1234`（6～64）。`existsByUsername("demo")` 则整次跳过（含记录）。
- 否则：save 用户（BCrypt）→ save 当前 profile（最新身高体重）→ `saveAll` 历史（每月一条，身高/体重随月微调）→ `saveAll` 日记录。
- 区间：上海「今天」往前约 90 天、往后约 90 天（闭区间按周生成，每周数条消耗+摄入，中英混合如 `跑步`/`easy run`/`燕麦`/`oats bowl`）。
- 禁止 for-loop 内 `save`/`selectById`。内存组装后各一次 `saveAll`。
- public `run` 打 `log.info`：入口 username、是否跳过、落库 userId 与 records/history size；禁止打印密码。

测试：`@ActiveProfiles("test")` 的集成测不应触发 seeder。可选窄测：无 `@ActiveProfiles("test")` 的切片过重则只测 `DemoDataSeeder` 纯方法或文档说明 skip；若加 `DemoDataSeederTest` 须 `@ActiveProfiles("test")` 以外的方式——**不**用 test profile 跑 seeder 打 SQLPub。种子行为以本地启动验证 + 代码审查 N+1。可加一个不连库的构造测试：给定 clock，生成的记录时间落在 ±90 天且含 CONSUME/INTAKE。

Clock：注入 `Clock` bean（若无则 `Clock.system(ZoneId.of("Asia/Shanghai"))`），便于测范围。

### D8: 文档

技术架构五期漏改：包表、mermaid、接口表、5.3 节从 CSV 改为 xlsx；补分享模块与 demo 种子一句。产品/功能去掉事项列的身体字段；分享改为二级页；报告回首页；demo 账号写 README（密码可写，这是公开测试账号，不是密钥）。

## Risks / Trade-offs

- [demo 密码写进文档] → 仅本地演示账号，非生产密钥；用户已要求给出账号密码
- [已存在 demo 用户则不刷新数据] → 避免每次启动重复插入；需清库或删用户才能重种
- [未来日期记录] → 日历按日能看到；符合「上下三个月」演示
- [复制 API 在非 HTTPS] → 失败时展示链接可手动复制

## Migration Plan

1. 前端路由与 CSS、xlsx 三列、种子、文档一并发布。
2. 回滚：还原本 change；已写入的 demo 用户可留。

## Open Questions

无。分享路由 `/calendar/share`；事项表头 `记录时间,类型,内容`；账号 `demo`/`demo1234`。
