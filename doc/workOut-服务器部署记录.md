# workOut 服务器部署记录

## 目标

- 将 workOut Docker 镜像（`linux/amd64`）传到服务器并启动
- 宿主机端口：**18080**（容器内仍监听 8080，映射 `18080:8080`）
- 设置 `WORKOUT_PUBLIC_BASE_URL=http://150.158.83.85:18080`（报告/分享链接基址）
- 按需确认/配置 Nginx 反代到 `127.0.0.1:18080`
- 健康检查通过后收尾

## 服务器与端口

| 项 | 值 |
| --- | --- |
| IP | 150.158.83.85 |
| 用户 | devops（凭证见 `.idea/服务器.md`） |
| SSH 端口 | 22 |
| 系统 | OpenCloudOS 9.6 / x86_64 |
| sudo | 免密 |
| 宿主机端口 | **18080**（勿用 8080） |
| 容器端口 | 8080 |
| PUBLIC_BASE_URL | `http://150.158.83.85:18080` |
| 镜像名 | `workout:amd64` |
| 容器名 | `workout-app` |

## 最终访问地址

- 应用：http://150.158.83.85:18080
- 健康检查：http://150.158.83.85:18080/api/v1/health
- 报告链接形态：`http://150.158.83.85:18080/report/{id}`

## 分步清单

### 0. Docker 构建加速（Maven 阿里云）

- [x] 新增 `backend/docker/maven-settings.xml`（阿里云 `maven.aliyun.com/repository/public`）
- [x] Dockerfile backend stage：`COPY` 到 `/root/.m2/settings.xml`，再执行 `mvn`
- [x] 停掉本机卡住的 amd64 模拟构建

### 1. 镜像构建（改在服务器原生 x86_64）

- [x] 检查既有后台构建：本机 `--platform linux/amd64` 卡在 Maven，已停止
- [x] rsync 同步代码到服务器 `~/workOut-build`（排除 `.git`、`node_modules`、`target` 等）
- [x] 服务器上 `sudo docker build -t workout:amd64 .` 成功（约 2.8 分钟；Maven go-offline ≈41s）
- [x] 确认镜像架构为 `amd64`（`Arch=amd64 Os=linux`）

### 2. 服务器环境确认

- [x] SSH 连通
- [x] Docker / Nginx 可用（Docker 28.0.1；Nginx 1.30.4 active）
- [x] 确认端口 18080 空闲
- [x] 确认现有容器不受影响（dify、open-webui 等）

### 3. 镜像传输与加载

- [x] ~~本机 save/load~~ 改为服务器本地构建，无需传输
- [x] 远端可见 `workout:amd64`（IMAGE ID `713daee51a59`，约 336MB）

### 4. 启动容器

- [x] 移除旧容器 `workout-app`（若存在）
- [x] `docker run` 启动：`-p 18080:8080`，`SPRING_PROFILES_ACTIVE=docker`，`WORKOUT_PUBLIC_BASE_URL=http://150.158.83.85:18080`
- [x] 容器状态为 running（healthy）
- [x] 容器 env 含正确 `WORKOUT_PUBLIC_BASE_URL=http://150.158.83.85:18080`

### 5. Nginx（按需）

- [x] 检查现有 Nginx 配置：无 `workout`/`18080` 相关反代
- [x] **直连 18080，暂不改 Nginx**（80 仍由现有站点占用）

### 6. 验证与收尾

- [x] 服务器本机：`curl -sS http://127.0.0.1:18080/api/v1/health` 含 UP
- [ ] 外网：`curl -sS http://150.158.83.85:18080/api/v1/health`（本机超时；宿主机 firewalld 未启用，疑似**云安全组未放行 18080**；需在云控制台放行 TCP 18080）
- [x] 本部署文档勾选与结果与真实一致
- [x] 飞书异步通知已触发（正文不含密码）

### 7. 历史报告地址基址

- [x] 查清落库方式：`work_out_share_report` 只存 `token`（及快照等），**不存完整 URL**；创建响应里的 `url` 由 `WORKOUT_PUBLIC_BASE_URL` + `/report/{token}` 运行时拼接
- [x] 扫库确认：`work_out_*` 无 url/link 列；`snapshot_json` 等文本字段无 `localhost:8080` / `/report/` / 旧基址；现有约 86 行分享记录无需 UPDATE
- [x] 结论：历史报告打开路径随容器 env `WORKOUT_PUBLIC_BASE_URL=http://150.158.83.85:18080` 生效，形态为 `http://150.158.83.85:18080/report/{id}`

## 关键操作命令摘要

SSH（密码见 `.idea/服务器.md`，推荐 SSH_ASKPASS）：

```bash
ASKPASS=$(mktemp)
cat > "$ASKPASS" <<'EOF'
#!/bin/bash
echo '<password-from-server-md>'
EOF
chmod 700 "$ASKPASS"
DISPLAY=:0 SSH_ASKPASS="$ASKPASS" SSH_ASKPASS_REQUIRE=force \
  ssh -o StrictHostKeyChecking=accept-new -o PreferredAuthentications=password -o PubkeyAuthentication=no -p 22 devops@150.158.83.85 '...'
```

同步代码到服务器后构建：

```bash
rsync -az --delete \
  --exclude '.git' --exclude 'node_modules' --exclude 'backend/target' \
  --exclude 'frontend/dist' --exclude '.idea' --exclude '.cursor' \
  -e 'ssh ...' ./ devops@150.158.83.85:~/workOut-build/

ssh ... 'cd ~/workOut-build && sudo docker build -t workout:amd64 .'
```

启动：

```bash
sudo docker rm -f workout-app 2>/dev/null || true
sudo docker run -d --name workout-app --restart unless-stopped \
  -p 18080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e WORKOUT_PUBLIC_BASE_URL=http://150.158.83.85:18080 \
  workout:amd64
```

健康检查：

```bash
curl -sS http://127.0.0.1:18080/api/v1/health
curl -sS http://150.158.83.85:18080/api/v1/health
```

## 执行过程与结果

| 时间 | 事项 | 结果 |
| --- | --- | --- |
| 已完成 | SSH / Docker / Nginx / 18080 | x86_64；Docker 28.0.1；Nginx active；18080 空闲 |
| 已完成 | Maven 改阿里云 | `backend/docker/maven-settings.xml` + Dockerfile COPY |
| 已停止 | 本机 amd64 模拟构建 | 卡在 Maven，已 kill |
| 已完成 | 服务器 docker build | `workout:amd64` 成功，amd64 |
| 已完成 | 启动 workout-app | `18080:8080`，PUBLIC_BASE_URL 正确，healthy |
| 已完成 | 健康检查（服务器本机） | `127.0.0.1:18080` 返回 `status":"UP"`，容器 healthy |
| 待处理 | 外网访问 18080 | 从外部 curl 超时；疑似云安全组未放行 TCP 18080（与已开放的 8088/18480 对比） |
| 已完成 | Nginx | 直连 18080，未改配置 |
| 已完成 | 历史报告 URL 基址 | 库内只存 token，未 UPDATE；依赖 WORKOUT_PUBLIC_BASE_URL |

## 失败记录（如有）

- 本机 `docker build --platform linux/amd64` 在未配置阿里云前，长时间卡在 `mvn dependency:go-offline`；已停止并改在服务器原生构建（配合阿里云镜像后约 41s 完成该步）。
- 外网访问 `http://150.158.83.85:18080` 目前不通（连接超时）。容器与本机 curl 正常；需在腾讯云/云厂商**安全组**放行入站 TCP **18080** 后复测。
