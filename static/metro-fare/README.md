# 上海地铁调价对照（静态页）

纯前端页面，与 workOut 主应用（Spring/React）**完全隔离**，使用独立 Docker 镜像部署。

## 本机构建与运行

在仓库根目录执行：

```bash
# 方式一：docker compose（推荐）
docker compose -f docker-compose.metro-fare.yml up -d --build

# 方式二：手动 build + run
docker build -t metro-fare:local -f static/metro-fare/Dockerfile static/metro-fare
docker run -d --name metro-fare --restart unless-stopped -p 8765:80 metro-fare:local
```

验证：

```bash
curl -sS -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8765/
curl -sS http://127.0.0.1:8765/ | head
# 浏览器打开 http://127.0.0.1:8765/
```

停止：

```bash
docker compose -f docker-compose.metro-fare.yml down
# 或：docker rm -f metro-fare
```

## 部署到服务器

与主应用 `workout-app`（端口 18080）互不影响。建议宿主机端口 **8765**。

完整运维流程（rsync、服务器 compose 构建、验证、排障）见：**[`doc/metro-fare-部署SOP.md`](../../doc/metro-fare-部署SOP.md)**。

摘要：同步 `static/metro-fare/` + `docker-compose.metro-fare.yml` 到服务器 `~/workOut-build/`，再执行：

```bash
cd ~/workOut-build
sudo docker compose -f docker-compose.metro-fare.yml up -d --build
curl -sS http://127.0.0.1:8765/ | head
```

外网访问需安全组放行 TCP **8765**。**不要**重建/打扰主容器 `workout-app`。

## 说明

- 镜像仅含 `index.html`、`app.js`、`fare.js`、`styles.css`、`network.json`
- **不要**把本目录并进仓库根 `Dockerfile` / `docker-compose.yml` 的默认路径
- 更新页面后重新 `build` 并替换容器即可
