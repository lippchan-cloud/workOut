#!/usr/bin/env python3
"""Send work status notifications to a Feishu bot webhook."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request


WEBHOOK_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/adf4ba91-5995-4c8a-b722-9e87a686064d"


def build_content(args: argparse.Namespace) -> str:
    lines = [
        f"【{args.title}】",
        f"工作目的：{args.purpose}",
        f"影响范围：{args.scope}",
        f"工作成果：{args.result}",
        f"验证结果：{args.verification}",
    ]
    if args.risk:
        lines.append(f"风险提示：{args.risk}")
    return "\n".join(lines)


def post_message(content: str) -> None:
    payload = {
        "msg_type": "text",
        "content": {
            "text": content,
        },
    }
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        WEBHOOK_URL,
        data=data,
        headers={"Content-Type": "application/json; charset=utf-8"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        body = response.read().decode("utf-8")
        if response.status >= 400:
            raise RuntimeError(f"Feishu webhook HTTP {response.status}: {body}")
        result = json.loads(body)
        if result.get("code") != 0:
            raise RuntimeError(f"Feishu webhook error: {body}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Notify Feishu bot about agent work status.")
    parser.add_argument("--title", required=True, help="Message title.")
    parser.add_argument("--purpose", required=True, help="Work purpose.")
    parser.add_argument("--scope", required=True, help="Affected scope.")
    parser.add_argument("--result", required=True, help="Work result.")
    parser.add_argument("--verification", required=True, help="Verification result.")
    parser.add_argument("--risk", default="", help="Remaining risk.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        post_message(build_content(args))
    except (urllib.error.URLError, TimeoutError, RuntimeError, json.JSONDecodeError) as exc:
        print(f"Failed to send Feishu notification: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

