#!/usr/bin/env bash
# Fire-and-forget Feishu notify: Base automation + custom bot webhook.
# Launcher always exits 0 immediately after spawning POSTs in the background.

set -u

# 多维表格 Base automation webhook
BASE_WEBHOOK_URL="https://jhcorp.feishu.cn/base/workflow/webhook/event/DpC0anDfCwslfnhbnbPcPzecnte"
# 群自定义机器人 bot/v2 webhook（需 msg_type）
BOT_WEBHOOK_URL="https://open.feishu.cn/open-apis/bot/v2/hook/8273b8cf-76ab-43b5-9fb7-07c7f7ac87ba"
# 兼容旧变量名
WEBHOOK_URL="${WEBHOOK_URL:-$BASE_WEBHOOK_URL}"

LOG_FILE="${FEISHU_DIALOG_NOTIFY_LOG:-/tmp/feishu-dialog-notify.log}"

PURPOSE="${PURPOSE:-}"
RESULT="${RESULT:-}"
PLAN="${PLAN:-}"
SYNC_MODE=0

usage() {
  cat <<'EOF'
Usage:
  send_async.sh --purpose TEXT --result TEXT --plan TEXT
  send_async.sh -p TEXT -r TEXT -a TEXT
  PURPOSE=... RESULT=... PLAN=... send_async.sh
  echo '{"purpose":"...","result":"...","plan":"..."}' | send_async.sh --stdin

Options:
  -p, --purpose   目的
  -r, --result    结果
  -a, --plan      执行方案
  --stdin         Read JSON from stdin with keys purpose/result/plan
  -h, --help      Show help

Always exits 0. Network work runs in background; failures append to LOG_FILE.

Posts to BOTH:
  1) Base automation JSON (目的/结果/执行方案 + purpose/result/plan + text)
  2) Custom bot webhook with msg_type=text
EOF
}

json_escape() {
  # Escape for JSON string value (no surrounding quotes)
  local s=$1
  s=${s//\\/\\\\}
  s=${s//\"/\\\"}
  s=${s//$'\n'/\\n}
  s=${s//$'\r'/\\r}
  s=${s//$'\t'/\\t}
  printf '%s' "$s"
}

parse_stdin_json() {
  # Minimal JSON extract without requiring jq
  local raw
  raw=$(cat)
  PURPOSE=$(printf '%s' "$raw" | sed -n 's/.*"purpose"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
  RESULT=$(printf '%s' "$raw" | sed -n 's/.*"result"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
  PLAN=$(printf '%s' "$raw" | sed -n 's/.*"plan"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -p|--purpose)
      PURPOSE="${2:-}"
      shift 2
      ;;
    -r|--result)
      RESULT="${2:-}"
      shift 2
      ;;
    -a|--plan)
      PLAN="${2:-}"
      shift 2
      ;;
    --stdin)
      SYNC_MODE=0
      parse_stdin_json
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      # Ignore unknown args; never fail the launcher
      shift
      ;;
  esac
done

# If still empty and stdin is a pipe with data, try JSON once
if [[ -z "$PURPOSE$RESULT$PLAN" ]] && [[ ! -t 0 ]]; then
  parse_stdin_json
fi

PURPOSE="${PURPOSE:-（未填写）}"
RESULT="${RESULT:-（未填写）}"
PLAN="${PLAN:-（未填写）}"

TEXT=$(printf '目的：\n%s\n结果：\n%s\n执行方案：\n%s' "$PURPOSE" "$RESULT" "$PLAN")

EP=$(json_escape "$PURPOSE")
ER=$(json_escape "$RESULT")
EA=$(json_escape "$PLAN")
ET=$(json_escape "$TEXT")

# Base automation: dual Chinese/English keys + combined text/content for flexible field mapping
BASE_PAYLOAD=$(printf '{"目的":"%s","结果":"%s","执行方案":"%s","purpose":"%s","result":"%s","plan":"%s","text":"%s","content":"%s"}' \
  "$EP" "$ER" "$EA" "$EP" "$ER" "$EA" "$ET" "$ET")

# Custom bot: open.feishu.cn bot/v2 requires msg_type (text)
BOT_PAYLOAD=$(printf '{"msg_type":"text","content":{"text":"%s"}}' "$ET")

# Background worker: never blocks the caller; log only; always silent to parent.
# Bypass local HTTP(S) proxies — Feishu webhooks often fail with
# "Proxy CONNECT aborted" / CONNECT 403 under corporate proxies.
(
  # Drop inherited proxy env so curl does not tunnel via a broken proxy
  unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY ALL_PROXY all_proxy no_proxy NO_PROXY
  export http_proxy= https_proxy= HTTP_PROXY= HTTPS_PROXY= ALL_PROXY= all_proxy=

  post_json() {
    local name=$1
    local url=$2
    local payload=$3
    local body_file=/tmp/feishu-dialog-notify-body.$$.${name}
    local http_code
    echo "[${name}] payload_bytes=${#payload}"
    http_code=$(curl -sS -o "$body_file" -w '%{http_code}' \
      --noproxy '*' \
      -x '' \
      -X POST "$url" \
      -H 'Content-Type: application/json' \
      -d "$payload" \
      --connect-timeout 5 \
      --max-time 15 \
      2>>"$LOG_FILE") || http_code="curl_error"
    echo "[${name}] http_code=${http_code}"
    if [[ -f "$body_file" ]]; then
      echo "[${name}] body=$(head -c 500 "$body_file")"
      rm -f "$body_file"
    fi
  }

  {
    echo "---- $(date '+%Y-%m-%d %H:%M:%S') ----"
    echo "proxy_bypass=1"
    post_json "base" "${WEBHOOK_URL:-$BASE_WEBHOOK_URL}" "$BASE_PAYLOAD"
    post_json "bot" "$BOT_WEBHOOK_URL" "$BOT_PAYLOAD"
  } >>"$LOG_FILE" 2>&1
) >/dev/null 2>&1 &
disown 2>/dev/null || true

exit 0
