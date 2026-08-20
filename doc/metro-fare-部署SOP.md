# 上海地铁调价页 — 独立镜像部署 SOP

## 1. 目的 / 范围

将 `static/metro-fare/` 静态页以**独立 Docker 镜像**构建、同步并部署到服务器，与主应用 `workout-app`（端口 18080、主 `Dockerfile` / `docker-compose.yml`）**完全隔离**。

| 项 | 值 |
| --- | --- |
| 容器名 | `metro-fare` |
| 镜像名 | `metro-fare:local`（compose 默认）；服务器亦可称 `metro-fare:amd64` |
| 端口映射 | **8765:80**（宿主机 8765 → 容器内 nginx 80） |
| Compose 文件 | 仓库根目录 `docker-compose.metro-fare.yml` |
| 公网访问 | `http://<服务器IP>:8765/`（需云安全组放行 TCP **8765**） |

**硬性约束：**

- 不要把本页并进仓库根默认 `Dockerfile` / `docker-compose.yml`
- 部署/更新时**不要**重建、重启或删除主应用容器 `workout-app`
- 本机若已有占用 8765 的服务（如本地 python 静态服务），先停掉再起容器

更细的页面说明见 `static/metro-fare/README.md`。主应用部署见 `doc/workOut-服务器部署记录.md`。

---

## 2. 前置条件

- 本机或服务器已安装 Docker，且支持 `docker compose`（Compose V2）
- 服务器为 **linux/amd64**（如 OpenCloudOS x86_64）；**优先在服务器上原生 build**，避免本机 Apple Silicon 跨架构模拟
- SSH 可登录服务器（用户/IP/凭证见本地服务器笔记，如 `.idea/服务器.md`；**本 SOP 不记录密码**）
- 服务器上已有或可创建目录 `~/workOut-build/`（与主应用代码同步目录可共用，仅操作 metro-fare 相关文件）
- 公网访问前：云厂商安全组放行入站 **TCP 8765**

---

## 3. 文件清单

| 路径 | 说明 |
| --- | --- |
| `static/metro-fare/Dockerfile` | nginx:alpine，仅 COPY 运行所需静态资源 |
| `static/metro-fare/index.html` | 页面入口 |
| `static/metro-fare/app.js` | 前端逻辑 |
| `static/metro-fare/fare.js` | 票价逻辑 |
| `static/metro-fare/styles.css` | 样式 |
| `static/metro-fare/network.json` | 线路网络数据 |
| `docker-compose.metro-fare.yml` | 独立 compose（`container_name: metro-fare`，`8765:80`） |

镜像**不包含**开发用文件（如 `build-network.py`、`amap-raw.json`）。

---

## 4. 本机构建验证（可选）

在仓库根目录执行。若本机 **8765 已被占用**，先停止占用进程（例如本地 python 服务），再执行：

```bash
cd /path/to/workOut

docker compose -f docker-compose.metro-fare.yml up -d --build
```

验证：

```bash
curl -sS -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8765/
curl -sS http://127.0.0.1:8765/ | head
# 浏览器：http://127.0.0.1:8765/
```

停止本机容器（**不影响**服务器上的 `metro-fare` / `workout-app`）：

```bash
docker compose -f docker-compose.metro-fare.yml down
```

---

## 5. 同步到服务器

只需同步地铁页相关文件到服务器 `~/workOut-build/`（不必整仓同步）。在**本机仓库根目录**执行（将 `<服务器IP>`、SSH 方式按实际替换）：

```bash
cd /path/to/workOut

# 确保远端有构建根目录
ssh devops@<服务器IP> 'mkdir -p ~/workOut-build/static/metro-fare'

# 同步静态页目录
rsync -az --delete \
  static/metro-fare/ \
  devops@<服务器IP>:~/workOut-build/static/metro-fare/

# 同步独立 compose（放在 workOut-build 根，与 Dockerfile 相对路径一致）
rsync -az \
  docker-compose.metro-fare.yml \
  devops@<服务器IP>:~/workOut-build/docker-compose.metro-fare.yml
```

说明：

- `--delete` 仅作用于 `static/metro-fare/`，避免远端残留已删除的静态资源
- 若本机本就在为 `workout-app` 做整仓 `rsync` 到 `~/workOut-build/`，上述文件会一并带上，可跳过本节单独同步
- **不要**在同步后误执行主应用的 `docker build` / `docker compose` 全量重建，除非明确要更新主应用

---

## 6. 服务器构建与启动（推荐）

登录服务器后，在 `~/workOut-build` 用 **compose 一键构建并启动**（优先服务器原生 amd64）：

```bash
ssh devops@<服务器IP>

cd ~/workOut-build
sudo docker compose -f docker-compose.metro-fare.yml up -d --build
```

预期：

- 构建镜像 `metro-fare:local`（compose 中 `image` 字段）
- 容器名 `metro-fare`，映射 `8765:80`，`restart: unless-stopped`
- **不会**触碰 `workout-app` 或其他无关容器

### 备选：手动 build + run（与 compose 等价）

```bash
cd ~/workOut-build
sudo docker build -t metro-fare:amd64 -f static/metro-fare/Dockerfile static/metro-fare
sudo docker rm -f metro-fare 2>/dev/null || true
sudo docker run -d --name metro-fare --restart unless-stopped \
  -p 8765:80 \
  metro-fare:amd64
```

不推荐默认走「本机跨架构 build 再 scp 镜像」；仅在服务器无法构建时再考虑（见 `static/metro-fare/README.md`）。

---

## 7. 验证清单

### 7.1 服务器本机

```bash
sudo docker ps --filter name=metro-fare
curl -sS -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8765/
curl -sS http://127.0.0.1:8765/ | head
```

期望：容器 `Up`（或 healthy）；HTTP 状态码 `200`；HTML 含地铁调价页内容。

### 7.2 确认主应用未受影响

```bash
sudo docker ps --filter name=workout-app
curl -sS http://127.0.0.1:18080/api/v1/health
```

### 7.3 公网与安全组

```bash
# 从外网机器或本机执行（IP 见服务器笔记 / 部署记录）
curl -sS -o /dev/null -w '%{http_code}\n' http://<服务器IP>:8765/
curl -sS http://<服务器IP>:8765/ | head
```

- 浏览器：`http://<服务器IP>:8765/`
- 若本机 curl 通、公网超时：检查云安全组是否放行 **TCP 8765**（与主应用 18080 同理，二者需分别放行）

---

## 8. 常用运维命令

均在服务器 `~/workOut-build` 或任意目录执行；**勿**对主 compose / `workout-app` 误操作。

```bash
# 日志
sudo docker logs -f --tail 100 metro-fare

# 重启（不重建镜像）
sudo docker restart metro-fare
# 或：
cd ~/workOut-build && sudo docker compose -f docker-compose.metro-fare.yml restart

# 停止并删除本容器（不影响 workout-app）
cd ~/workOut-build && sudo docker compose -f docker-compose.metro-fare.yml down
# 或：sudo docker rm -f metro-fare

# 更新发布（改完静态资源并 rsync 之后）
cd ~/workOut-build
sudo docker compose -f docker-compose.metro-fare.yml up -d --build
```

---

## 9. 故障排查

| 现象 | 可能原因 | 处理 |
| --- | --- | --- |
| `bind: address already in use` / 8765 起不来 | 宿主机 8765 被占用（本机常见：python 静态服务；服务器：旧进程或其他容器） | `ss -lntp \| grep 8765` 或 `sudo lsof -i :8765`；停掉占用方后再 `up` |
| 本机构建极慢 / 架构不符 | Apple Silicon 模拟 `linux/amd64` | **改在服务器原生 build**（本 SOP 第 6 节） |
| 公网 curl 超时，服务器 `127.0.0.1:8765` 正常 | 云安全组未放行 TCP 8765 | 在云控制台添加入站规则后复测 |
| 页面 404 / nginx 欢迎页 | 镜像未正确 COPY 静态文件，或挂错目录 | 确认 Dockerfile 的 COPY 列表；`sudo docker exec metro-fare ls /usr/share/nginx/html` |
| 误伤主应用 | 对根目录默认 compose / `workout-app` 执行了 `down`/`rm`/`build` | 地铁页**只用** `-f docker-compose.metro-fare.yml` 或容器名 `metro-fare` |
| rsync 后 compose 找不到 context | compose 不在 `~/workOut-build` 根，或未同步 `static/metro-fare` | 按第 5 节核对远端路径后再 `up --build` |

---

## 10. 变更记录（摘要）

- 首次独立镜像：服务器 `~/workOut-build` + `docker compose -f docker-compose.metro-fare.yml up -d --build`（或等价 `docker build`/`run`），容器 `metro-fare`，端口 `8765:80`
- 与 `workout-app:18080` 并行运行、互不依赖
