## Context

四期已有：`work_out_profile_history`（`changedAt` = `Instant.now()`）、`GET /api/v1/profile/trends`、CSV 导出（UTF-8 BOM + 身体列）、日历二级 `/calendar/trends` SVG。增量做在未提交的二/三/四期之上，不还原。约束：TDD；Java `java-architecture-master`（public 方法 `log.info`、禁止 N+1）；时区 Asia/Shanghai；曲线不加沉重图表库。

## Goals / Non-Goals

**Goals:**

- 曲线：标准单位 + 时间轴 + pan + 粒度 zoom；迁到「我的」身体资料页下方
- 资料真实日期写入 `changedAt`
- 导出 xlsx 双工作表；分享 H5；二者均校验身高体重
- 公开报告页：范围 + 三块内容 + 建议分析占位
- 文档与 OpenSpec 同步

**Non-Goals:**

- 组织 / SSO / 多环境拆分
- 真实 AI/医疗建议文案
- 新图表 npm 依赖
- 把分享 id 做成可遍历的自增主键
- commit / push

## Decisions

### D1: 导出用 xlsx（Apache POI），不坚持 CSV 多分 sheet

**Choice:** 真正 CSV 没有多工作表。优先单个 `.xlsx`，不用 zip 多 csv。依赖 `org.apache.poi:poi-ooxml`（钉版本，如 5.2.5）。

- Sheet1 名称「事项列表」：列仍为 `记录时间,类型,内容,昵称,身高cm,体重kg`，按 `recordedAt` 对齐历史。
- Sheet2 名称「成长曲线」：列 `时间,身高cm,体重kg`，行为该用户历史点（时间、单位明确）。

**路径：** 保持 `GET /api/v1/dailyRecords/exportCsv`（避免日历 fetch URL 大改）。**BREAKING 格式：** Content-Type 改为 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`；文件名 `.xlsx`（`RecordQueryPeriod` 增加 `xlsxFilename()`）。入口文案「导出」（用户仍可口头叫 csv）。

**Alternatives:** zip 内两个 csv；坚持单 csv 无法满足「另外一个工作表」。

### D2: 分享 URL 用 `/report/:id`，base 可配置

**Choice:** React Router 用 `/report/:id`（比字面 `/report/id=233232` 更自然）。用户示例中的 `id=` 是「查询串风格」；实现用路径参数，文档说明等价含义。公开 id 是**随机 token**（UUID 去横线或 22+ 字符），不是表自增主键，防止遍历别人报告。

拼接：`{WORKOUT_PUBLIC_BASE_URL}/report/{token}`。配置：

```yaml
workout:
  public-base-url: ${WORKOUT_PUBLIC_BASE_URL:http://localhost:8080}
```

默认 localhost，**不要写死** `192.168.9.0`。创建接口返回 `{ id, url }`，`id` 即 token。

**Alternatives:** `/report?id=`；path 字面 `id=233232`。后者对 SPA fallback 与 Router 都不友好。

### D3: 分享快照 JSON，公开读不查别人库行

**Choice:** 表 `work_out_share_report`：

```
id BIGINT PK
token VARCHAR(64) UNIQUE NOT NULL
user_id BIGINT NOT NULL
range_from DATE NOT NULL
range_to DATE NOT NULL
snapshot_json MEDIUMTEXT NOT NULL
created_at TIMESTAMP(3) NOT NULL
```

创建时一次查出：当前资料显示名、区间事项、成长曲线点，写入 snapshot（禁止 N+1）。`GET /api/v1/reports/{id}` **permitAll**，只按 token 取快照；未知 token → 404「报告不存在」。不把 userId 暴露给前端。注销时 `deleteByUserId` 批量删分享行。

快照结构（camelCase）：

```json
{
  "from": "2026-08-01",
  "to": "2026-08-18",
  "displayName": "小明",
  "records": [{ "recordedAt": "...", "type": "CONSUME", "content": "跑步" }],
  "bodyHistory": [{ "changedAt": "...", "heightCm": 175.0, "weightKg": 70.0 }],
  "advice": null
}
```

`advice` 恒为空，报告页第四块空态「建议分析（即将提供）」。

**API：** `POST /api/v1/shareReports`（JWT，筛选参数与导出相同）。

**Alternatives:** 读时现查库。快照避免未登录路径碰业务表权限分叉，且分享时刻冻结。

### D4: 身高体重闸门（前后端都要）

**Choice:** 当前 `work_out_profile` 的 `height_cm` **与** `weight_kg` 均非空才允许导出/分享。缺任一：后端 `BusinessException`「请先填写身高和体重」（HTTP 400）。前端点击导出/分享前 `GET /api/v1/profile`，缺则拦截、提示并引导 `/profile/body`，不发起下载/创建分享。

**Alternatives:** 只前端拦（可被绕过）；用历史最后一条代替当前资料（用户说的是「填写资料」当前值）。

### D5: 资料真实日期 = 请求 `changedAt`

**Choice:** `PUT /api/v1/profile` 增加可选 `changedAt`（ISO Instant）。缺省服务器 now。前端 `datetime-local` 默认此刻（上海），提交转 ISO。有字段变化时历史行的 `changedAt` 用该值；`work_out_profile.updated_at` 同步为该值（「当前资料」时间）。无字段变化仍不追加历史。

校验：不可明显未来（允许数分钟时钟偏差，如 +5 分钟）。

**Alternatives:** 只改历史、当前行仍 now。用户要求写入历史与当前 profile。

### D6: 曲线迁入身体资料页；日历主路径去掉入口

**Choice:** `/profile/body` 上表单（含真实日期）+ 下成长曲线。复用 `GET /api/v1/profile/trends`。日历去掉「变化曲线」按钮；可保留路由 `/calendar/trends` 重定向到 `/profile/body` 以免旧链失效（测试改到资料页）。一级 Tab「我的」高亮。

曲线 UI：现有卡片 + 绿/红体系的描边与刻度，不高饱和渐变、不用 emoji。SVG 自绘。

### D7: pan / zoom 改时间窗口与刻度粒度

**Choice:** 状态：`precision` ∈ `{hour, day, week, month}` + 可见时间窗口 `[viewStart, viewEnd]`。

- **Zoom in**：精度变细（month → week → day → hour），必要时缩短窗口以匹配刻度。
- **Zoom out**：精度变粗。
- **Pan**：指针/触摸水平拖动平移 `viewStart/viewEnd`，不改变精度。
- X 轴标签按精度格式化（hour: `MM-DD HH:mm`；day: `MM-DD`；week: 该周一起；month: `YYYY-MM`）。
- Y 轴标注单位 `cm` 或 `kg`（系列切换保留）。
- **禁止**对 SVG 做 CSS `transform: scale` 当放大。

点按实际 `changedAt` 映射到时间轴，不是等距 index。数据少时窗口包住全部点；点多时可拖。

控件：「放大」「缩小」按钮（可配合滚轮）。`role="img"` aria 含单位。

### D8: 报告页独立于三 Tab

**Choice:** `/report/:id` 与 `/cms` 一样不包 `AppShell`。公开 GET 报告；展示范围文案 + 三块上下排列 + 第四块建议分析空态。曲线只读（可同样 pan/zoom 与单位）。SPA fallback 增加 `/report`、`/report/**`。Security：`/api/v1/reports/**` permitAll。

### D9: 包布局

- `modules.share.api|application|infrastructure` 新模块（分享创建 + 公开读）
- 导出 xlsx 仍在 `DailyRecordService`（或抽出 `WorkbookExportService` 避免类膨胀）
- `WorkoutPublicProperties`：`workout.public-base-url`
- Flyway `V5__share_report.sql`

TDD：后端 MockMvc 窄测（xlsx 用 POI 读回 sheet；分享无 token 可读；缺身高 400）；前端 Vitest（资料页曲线、导出拦截、报告页）。SQLPub 全量 `mvn test` 不打满。

## Risks / Trade-offs

- [旧客户端仍期望 CSV 文本] → 文档标明 BREAKING；路径名 `exportCsv` 保留但 MIME 为 xlsx
- [POI 包体积] → 可接受；不用 zip+csv 双实现
- [快照与之后改资料不一致] → 有意冻结；新分享才含新数据
- [token 泄漏即可见该范围数据] → 随机不可遍历；不放密码；范围由用户选择
- [datetime-local 无时区] → 前端按本地（上海）编 ISO offset
- [曲线测试难测拖拽] → 测单位文案、放大后刻度变化（data 属性或轴文本）、资料页存在曲线；pan 可用 fireEvent pointer

## Migration Plan

1. Flyway V5 建分享表；pom 加 poi-ooxml。
2. 发布后端（xlsx、changedAt、分享、公开 GET）与前端（资料页曲线、导出/分享、报告页）。
3. 回滚：还原本 change；分享表可留。

## Open Questions

无。导出文案「导出」；分享链接形态 `/report/:id`；base 配置项名 `WORKOUT_PUBLIC_BASE_URL`。
