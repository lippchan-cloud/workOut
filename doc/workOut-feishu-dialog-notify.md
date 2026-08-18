---
name: feishu-dialog-notify
description: >-
  Asynchronously posts Cursor conversation turn summaries to Feishu (Lark) in a
  fixed Chinese three-section format (目的 / 结果 / 执行方案): both Base
  (多维表格) automation webhook AND custom bot/v2 hook (msg_type=text).
  Auto-applies for all chats where this skill is available: after completing a
  meaningful assistant turn, the agent SHOULD fire-and-forget notify without
  waiting for the user to attach or @-mention this skill. Also use when the
  user asks to sync/push dialog to Feishu. Trigger terms: 飞书、Lark、webhook、
  对话同步、异步推送、对话摘要、同步到飞书、自动推送.
---

# Feishu Dialog Notify

Fire-and-forget push of the current turn summary to Feishu Base automation.
User reply is never blocked by send success/failure.

**Auto-select:** Do not require the user to manually attach or pick this skill. When the skill is discoverable (personal `~/.cursor/skills` or project `.cursor/skills`), invoke it after meaningful turns.

## When to run

- After completing a **meaningful** assistant turn (implementation, investigation, design decision, debug fix, etc.) — **default, no user attach needed**
- User explicitly asks to sync / push / 同步对话到飞书

Skip trivial turns (pure acks, clarifying one-liners with no substantive work). Prefer one notify per meaningful turn.

## Message format (exact headings, Chinese, this order)

```
目的：
结果：
执行方案：
```

Fill from the **current** conversation turn only:

| Section | Content |
|---------|---------|
| 目的 | What the user wanted |
| 结果 | What was completed / decided |
| 执行方案 | How it was done (brief steps) |

Keep each section concise (a few sentences or short bullets). Never put secrets, passwords, tokens, API keys, or webhook URLs in the message.

## How to send (required)

Use the bundled script — do not invent curl each time.

```bash
./doc/scripts/send_async.sh \
  --purpose "用户请求摘要" \
  --result "完成了什么" \
  --plan "怎么做的"
```

### Fire-and-forget (mandatory)

1. Invoke the script with Shell `block_until_ms: 0` (background), **or** rely on the script's own backgrounding (it exits 0 immediately).
2. **Shell 必须带 `required_permissions: ["all"]`**（或至少能直连外网的等价权限）。Cursor 沙箱/代理会导致 `jhcorp.feishu.cn` DNS 失败或 `Proxy CONNECT aborted`；脚本已对 curl 做 `--noproxy '*'` 清代理，但仍需非沙箱网络才能送达。
3. **Do not await** HTTP success. Do not poll the log. Do not retry in the foreground.
4. Reply to the user normally. Feishu is side-channel only.
5. Never mention Feishu send failure as blocking the task. Never fail the conversation because of notify.

### Proxy / network note

- Script worker **clears** `http(s)_proxy` / `ALL_PROXY` and uses `curl --noproxy '*' -x ''` so corporate proxies cannot intercept the webhook.
- Agent-side: always request unrestricted network (`all`) when calling this script; sandbox allowlist alone is not enough for Feishu.

## Script CLI

```text
send_async.sh --purpose TEXT --result TEXT --plan TEXT
send_async.sh -p TEXT -r TEXT -a TEXT          # short flags (-a = plan/方案)
echo via env: PURPOSE / RESULT / PLAN
```

Also accepts stdin JSON: `{"purpose":"...","result":"...","plan":"..."}`.

Launcher always exits 0. Optional logs: `/tmp/feishu-dialog-notify.log`.

## Dual destinations (both required)

每次调用脚本会 **同时** POST 到两处（脚本内已固化 URL；Agent 勿手写 curl）：

1. **Base automation**（多维表格）— 结构化字段 JSON  
2. **Custom bot**（`open.feishu.cn/.../bot/v2/hook/...`）— `msg_type: text`

### Payload shape (Base automation)

```json
{
  "目的": "...",
  "结果": "...",
  "执行方案": "...",
  "purpose": "...",
  "result": "...",
  "plan": "...",
  "text": "目的：...\n结果：...\n执行方案：...",
  "content": "目的：...\n结果：...\n执行方案：..."
}
```

**Caveat:** Feishu Base automation must map these JSON keys to variables / table fields on the Base side. If mapping is missing, the webhook may accept the request but not write rows as expected.

### Payload shape (Custom bot)

Bot hook 缺少 `msg_type` 会返回 `params error, msg_type need`。脚本固定发送：

```json
{
  "msg_type": "text",
  "content": {
    "text": "目的：...\n结果：...\n执行方案：..."
  }
}
```

## Rules

1. After a meaningful turn (or on user sync ask), fill 目的 / 结果 / 执行方案, then call the script async — **without requiring manual skill selection**.
2. Never wait for Feishu; never surface send errors as conversation failures.
3. Never include secrets in the Feishu body.
4. Always use the bundled script（双通道 Base + bot）；不要只推其一，也不要手拼 bot payload。
5. User-facing reply is independent of whether Feishu received the message.

## Optional

- More CLI examples: [workOut-feishu-dialog-notify-examples.md](workOut-feishu-dialog-notify-examples.md)
