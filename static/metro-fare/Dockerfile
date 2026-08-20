# 上海地铁调价静态页 — 独立轻量镜像（与 workOut 主应用无关）
# 用法（在仓库根目录）：
#   docker build -t metro-fare:local -f static/metro-fare/Dockerfile static/metro-fare
#   docker run --rm -p 8765:80 metro-fare:local
# 或：docker compose -f docker-compose.metro-fare.yml up -d --build

FROM nginx:1.27-alpine

# 只托管运行所需静态资源（不含 build-network.py / amap-raw.json）
# COPY 会覆盖 nginx 默认 welcome 页
COPY index.html app.js fare.js styles.css network.json /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1
