# Examples

## Typical agent invoke

```bash
./doc/scripts/send_async.sh \
  --purpose "为对话摘要创建飞书异步推送 Skill" \
  --result "已落地 SKILL.md 与 send_async.sh，并完成 webhook 试发" \
  --plan "按 create-skill 规范写入个人技能目录；脚本后台 curl POST Base automation JSON"
```

## Short flags

```bash
./doc/scripts/send_async.sh \
  -p "修复登录超时" \
  -r "已调整 token 刷新逻辑并验证通过" \
  -a "定位超时点 → 改刷新策略 → 本地回归"
```

## Env vars

```bash
PURPOSE="同步本轮结论" \
RESULT="用户确认方案 A" \
PLAN="整理要点后异步推送飞书" \
  ./doc/scripts/send_async.sh
```

## Stdin JSON

```bash
printf '%s' '{"purpose":"试发","result":"ok","plan":"curl Base JSON"}' \
  | ./doc/scripts/send_async.sh --stdin
```

## Payload note

Body is Base automation JSON (`目的`/`结果`/`执行方案` + `purpose`/`result`/`plan` + `text`/`content`), not bot `msg_type`. Map keys in Feishu Base automation if needed.

## Agent Shell settings

- Prefer `block_until_ms: 0` when calling the script.
- Do not read `/tmp/feishu-dialog-notify.log` unless the user asks to debug notify.
- Continue the user-facing reply immediately after spawning.
