#!/usr/bin/env bash
# 默认 DB / JWT 已写入 backend/src/main/resources/application.yml（私有仓）。
# WORKOUT_DB_* / WORKOUT_JWT_SECRET 为可选覆盖，不设也可启动。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/frontend"
npm run build:static
cd "$ROOT/backend"
mvn spring-boot:run
