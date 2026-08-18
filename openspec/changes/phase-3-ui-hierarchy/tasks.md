# Tasks: phase-3-ui-hierarchy

> **TDD 强制规则（每个实现子任务）：**
> 1) 先写失败测试 → 2) 运行确认失败原因正确 → 3) 写最小实现 → 4) 运行确认通过 → 5) 再勾选。
> 禁止先写生产代码再补测试。
>
> **验证门禁：**
> - 规范：[`doc/workOut-TDD规范.md`](../../../doc/workOut-TDD规范.md)
> - 台账：[`doc/workOut-TDD验证记录.md`](../../../doc/workOut-TDD验证记录.md)
> - **未在验证记录中留下 RED 命令输出摘要 + GREEN 通过摘要，不得勾选本文件对应项。**

Using change: `phase-3-ui-hierarchy`. Override with `/opsx:apply <other>`.

## 1. GET record by id (backend)

- [x] 1.1 **TDD** `DailyRecordGetByIdTest`：所有者 `GET /api/v1/dailyRecords/{id}` 返回 200 且 content/type 正确；跨用户与不存在 id 返回 404「记录不存在」。RED → Controller/Service `getById` 复用 `requireOwned` → GREEN。写入验证记录

## 2. 「我的」二级 / 三级 (frontend)

- [x] 2.1 **TDD** `ProfilePage.test.tsx`：`/profile` 只出现身体资料 / 账号安全 / 退出登录，不出现身高输入与修改密码。点身体资料进 `/profile/body` 可保存；点账号安全进 `/profile/account` 有改密与注销确认；返回回到选项层；选项层可退出登录。RED → 拆路由与页面 → GREEN
- [x] 2.2 **TDD** ADMIN 在 `/profile/account` 可见「后台管理」；USER 不可见。RED → 账号页 CMS 入口 → GREEN

## 3. 日历周控件、气泡、详情 (frontend)

- [x] 3.1 **TDD** 周工具：上一周/下一周带 `week-nav-btn`；切周仍改变周条。日格子 `week-day` 可 focus。RED → 样式与结构 → GREEN
- [x] 3.2 **TDD** 日模式一次 `from&to` 拉取当周；有 2 条记录的日子显示气泡 `2`；零记录无数字气泡。RED → 前端聚合 count → GREEN
- [x] 3.3 **TDD** 点击列表行进 `/calendar/records/:id` 并请求 GET by id；详情有编辑/删除；删除需确认；返回选中该记录日。RED → 详情页 + 路由 → GREEN
- [x] 3.4 **TDD** 「补记」仍带 date 进表单，且不是 `btn-record-hero` / `btn-block`。RED → 补记次要样式 → GREEN

## 4. 按钮层级

- [x] 4.1 调整 `index.css`：主 CTA 实色、次要 ghost、文字/周导航小按钮；记录大按钮圆角与触控高度更精致。首页「开始记录」回归仍通过既有测试

## 5. 文档与 specs sync

- [x] 5.1 更新 `doc/workOut-产品文档.md` 信息架构与用户路径（我的三级、日历气泡/详情）
- [x] 5.2 更新 `doc/workOut-功能文档.md` 页面/字段/交互
- [x] 5.3 README 若仍写「我的一页填完」则改一句
- [x] 5.4 按 openspec-sync-specs 把 delta 写入 `openspec/specs/`

## 6. 回归与收尾

- [x] 6.1 跑相关前后端测试并写入验证记录
- [x] 6.2 `openspec validate --change phase-3-ui-hierarchy`（若 CLI 支持）
- [x] 6.3 **不 commit、不 push**
