## Context

Phase 3 已有：日历周条 + 月/区间列表 + 详情 URL、CSV 按筛选导出（列：记录时间,类型,内容）、「我的」身体资料 upsert 只改 `work_out_profile` 当前行。月列表只渲染内容；CSV 不带身高体重；改资料无历史。

约束：增量做在未提交的二/三期之上，不还原既有能力；TDD；Java `java-architecture-master`（public 方法 `log.info`、禁止 N+1）；时区 Asia/Shanghai；曲线页不加沉重图表库。

## Goals / Non-Goals

**Goals:**

- 列表每条可见 `recordedAt` 时分（月视图必须；日/区间一致）
- 身体变更写入 `work_out_profile_history`；CSV 按事项时间对齐当时快照
- 日历二级「变化曲线」页，返回日历
- 文档与 OpenSpec 同步

**Non-Goals:**

- 组织 / SSO / 多环境 / 卡路里或医疗建议
- 新图表 npm 依赖
- 用当前资料覆盖历史天的 CSV
- commit / push

## Decisions

### D1: 历史表存「变更后快照」而非只存 diff

**Choice:** `work_out_profile_history(user_id, changed_at, nickname, height_cm, weight_kg)`。每次资料相对上一快照有字段变化时插入一行（新值快照）。`changed_at` 用服务端 `Instant.now()`。

**Alternatives:** 只存 changed fields。快照匹配 CSV 更简单：找 `changed_at <= recordedAt` 的最后一行。

### D2: 当时身体信息 = 最晚且不晚于 recordedAt 的快照

**Choice:** 导出时：一次查出该用户全部历史（或 `changed_at <= period.endExclusive` 且含 period 前最后一条），在内存按时间升序双指针/二分匹配每条记录。禁止按记录循环查库。

无匹配快照：身高/体重/昵称列留空。

**Alternatives:** 永远用当前 `work_out_profile`。违反需求。

### D3: CSV 表头扩展，URL 不变

**Choice:** 表头 `记录时间,类型,内容,昵称,身高cm,体重kg`。仍 UTF-8 BOM。空期间仅表头。既有 `exportCsv` 查询参数不变。

### D4: 曲线数据一个 GET

**Choice:** `GET /api/v1/profile/trends`（JWT）：

```json
{
  "bodyHistory": [{ "changedAt": "...", "nickname": "...", "heightCm": 175.0, "weightKg": 70.0 }],
  "recordCounts": [{ "date": "2026-08-18", "count": 2 }]
}
```

`recordCounts` 用一条 `GROUP BY` 上海自然日聚合本人未删除记录，禁止 N+1。无历史且无记录 → 空数组，前端空态。

另可用同一仓储支撑测试：保存资料后 trends 的 `bodyHistory` 增长。

**Alternatives:** 前端拉全部 dailyRecords 自己聚合。受 366 天区间限制且浪费。

### D5: 曲线页是日历二级，不是月网格同屏

**Choice:** 路由 `/calendar/trends`。日历页次要入口「变化曲线」。返回到 `/calendar`。一级 Tab 仍高亮「日历」。SPA fallback 已有 `/calendar/**`。SVG polyline，身高/体重切换（或双折线），不新增 chart 库。

### D6: 列表时间只改前端展示

**Choice:** list DTO 已有 ISO `recordedAt`。前端用 Asia/Shanghai 格式化为 `HH:mm`（日模式）或 `MM-DD HH:mm`（月/区间）。详情页已有完整时间，保持。

### D7: 现有资料回填一行历史

**Choice:** Flyway `V4` 建表后 `INSERT ... SELECT` 从 `work_out_profile` 用 `updated_at` 作为 `changed_at`，使旧用户导出也能对齐。

### D8: 注销批量删历史

**Choice:** `ProfileHistoryRepository.deleteByUserId`；`AuthService.deleteMe` 在删 profile 前/后一次调用，禁止循环。

## Risks / Trade-offs

- [CSV 表头变更导致旧测试断言失败] → 同期更新 `CsvExportTest` 表头期望；新测覆盖对齐
- [历史全量对超长用户偏大] → MVP 个人账本可接受；若将来再分页
- [测试里两次 PUT 间隔可能同秒] → 断言按顺序与不同身高值，必要时 `Thread.sleep` 或注入 clock；优先靠不同字段值区分行
- [曲线纵轴量纲不同] → 默认切换系列，避免身高厘米与体重千克硬叠在同一刻度造成误读；提供「身高 / 体重」切换，可选同时看两条（独立归一化或双轴标注）

## Migration Plan

1. Flyway V4 建 `work_out_profile_history` 并回填当前资料。
2. 发布后端（写历史 + 新 CSV 列 + trends）与前端（时间、曲线页）。
3. 回滚：还原本 change；表可保留（不影响旧代码读当前 profile）。

## Open Questions

无。入口文案固定「变化曲线」；CSV 列名固定如上。
