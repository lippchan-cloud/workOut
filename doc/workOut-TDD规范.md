# workOut TDD 规范与验证门禁

| 项 | 内容 |
| --- | --- |
| 产品名称 | workOut |
| 文档类型 | TDD 规范 |
| 文档版本 | v1.0 |
| 日期 | 2026-08-18 |
| 配套 | [功能文档](./workOut-功能文档.md)、[OpenSpec tasks](../openspec/changes/init-workout-mvp/tasks.md)、[TDD 验证记录](./workOut-TDD验证记录.md) |
| 原则来源 | Superpowers `test-driven-development`：先失败测试，再最小实现 |

---

## 0. 一句话

> **TDD ≠「有测试类」**。TDD = **红（可证明失败原因正确）→ 绿（可证明通过）→ 重构（保持绿）**，且每一步有命令与输出留证。

实现阶段必须以本规范为准；OpenSpec `tasks.md` 勾选前必须满足下方门禁。

---

## 1. 什么算「做完一个 TDD 任务」

对每一个可勾选任务（尤其标注 **TDD** 的项），必须完成下列五步，缺一不可：

| 步骤 | 动作 | 验证门禁（必须留下证据） |
| --- | --- | --- |
| R1 | 只写测试（或先写断言），**不写**对应生产实现 | 文件 diff 中生产代码未引入该行为 |
| R2 | 运行测试，确认 **失败** | 命令 + 失败摘要：断言失败 / 缺类 / 404 等，且原因与「功能未实现」一致 |
| G1 | 写**最小**生产代码使该测试通过 | 不顺便做无关重构或下一功能 |
| G2 | 再跑同一命令，确认 **通过** | 命令 + `Tests run: N, Failures: 0`（或等价） |
| G3 | （可选）小重构，再次全绿 | 若重构，再跑相关测试 |

**禁止：**

- 先实现再补测试（测试一上来就绿 → **作废**，删除实现重来，或补写能先红的新断言）
- 只写测试类、从不跑红/绿
- 用「我手动点过」代替自动化红绿证据
- 失败原因是编译错误、路径写错、环境配错却当成「RED 成功」（应先修测试/环境，再进入 RED）

---

## 2. 证据格式（写入验证记录）

每条任务在 [workOut-TDD验证记录.md](./workOut-TDD验证记录.md) 增加一节，模板如下：

```markdown
### Task X.Y — <名称>

- 对应规格：openspec/.../specs/<capability>/spec.md — Requirement: ...
- 测试类/文件：`...`
- RED 命令：`cd backend && mvn -q test -Dtest=FooTest`
- RED 结果：FAIL — <一句话失败原因>
- GREEN 命令：（同上）
- GREEN 结果：PASS — Tests run: N, Failures: 0
- 实现要点：<最小改动文件列表>
- 勾选：tasks.md X.Y
```

前端同理，命令示例：`cd frontend && npm test -- Foo.test.tsx`。

---

## 3. 与 OpenSpec / 产品文档的关系

```
产品/功能文档     →  用户价值与规则
OpenSpec specs    →  SHALL + Scenario（可测需求）
本 TDD 规范       →  如何把 Scenario 变成「先红后绿」
tasks.md          →  执行清单（勾选需验证记录）
TDD 验证记录      →  红绿证据台账（本仓库可审计）
```

每个 Scenario 理想对应至少一个自动化测试方法；方法名应能读出行为（如 `duplicateUsernameShouldReturn400`）。

---

## 4. 后端约定（Java）

| 项 | 约定 |
| --- | --- |
| 框架 | JUnit 5 + Spring Boot Test + MockMvc |
| 测试库 | SQLPub MySQL（`application-test.yml`，与主库同实例）+ Flyway；用例用 UUID 后缀用户名隔离 |
| 窄测命令 | `mvn -q test -Dtest=ClassName` 或 `ClassName#method` |
| 身份 | 业务测试不得信任客户端 `userId`；JWT 场景用签发测试 Token |
| 日志 | 生产代码遵循 `java-architecture-master`：public 入口 `log.info`（脱敏） |
| 注释 | 新增类/方法 JavaDoc；关键调用行注释 |

脚手架任务（如 1.1 建工程）可不跑 RED，但须在验证记录标注 **「脚手架，非行为 TDD」**，且不得借脚手架塞入业务行为。

---

## 5. 前端约定（React）

| 项 | 约定 |
| --- | --- |
| 框架 | Vitest + React Testing Library |
| 守卫/跳转 | 先写失败用例（无 token 点 Tab 未跳转）再写 Auth 守卫 |
| 颜色/文案 | 断言用户可见文本或约定 class/style，避免测实现细节 |

---

## 6. Agent / 人工执行检查单（每个 TDD 任务结束前）

- [ ] 是否先看到过 **RED**（命令输出已粘贴或摘录到验证记录）？
- [ ] RED 失败原因是否为「缺功能/断言不满足」，而非环境噪声？
- [ ] GREEN 是否同一测试命令通过？
- [ ] 是否只做了让当前测试通过的最小改动？
- [ ] `tasks.md` 对应项是否在验证记录写完后才勾选？
- [ ] 是否跑过相关回归（至少同模块已有测试）？

任一为否 → **任务未完成**，不得勾选。

---

## 7. 当前实现状态纠偏（2026-08-18）

此前在分支 `feat/init-workout-mvp` 上已有部分后端草稿，**不完全符合本规范的台账要求**。纠偏规则：

1. 已有明确 RED→GREEN 证据的（如 `FlywayMigrationTest`、`ApiResponseEnvelopeTest`）→ 补记验证记录后可勾选。
2. 仅有失败测试、实现未完成的（如 `AuthRegisterTest`）→ 保留测试为 RED，**暂停继续堆生产代码**，先补齐本规范与台账，再按门禁继续。
3. 若发现「先写实现后补测且从未红过」→ 删除或改写测试使其先红，再最小实现。

**在你确认「按 TDD 规范继续实现」之前，本仓库以补文档与台账为优先，不以赶功能为优先。**

---

## 8. 推荐执行口令

对 Agent 说：

> 按 `doc/workOut-TDD规范.md` 执行 OpenSpec Task X.Y：先红留证，再绿留证，写入 `doc/workOut-TDD验证记录.md`，最后勾选 `tasks.md`。
