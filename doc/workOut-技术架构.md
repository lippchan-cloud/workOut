# workOut 技术架构与流程

| 项 | 内容 |
| --- | --- |
| 产品名称 | workOut |
| 文档类型 | 技术架构 |
| 文档版本 | v1.4 |
| 日期 | 2026-08-18 |
| 依据 | [workOut-产品文档.md](./workOut-产品文档.md)、[workOut-功能文档.md](./workOut-功能文档.md)、`README.md` |
| 实现规格 | OpenSpec [`init-workout-mvp`](../openspec/changes/init-workout-mvp/design.md)、[`phase-6-share-export-demo`](../openspec/changes/phase-6-share-export-demo/) |

---

## 1. 总体技术架构

单体、前后端不分离：React 构建产物由 Spring Boot 静态资源托管；业务 API 同进程提供；数据落 MySQL。鉴权采用 **JWT**；业务数据按 `userId` 隔离。无微服务拆分。

```mermaid
flowchart TB
  subgraph Client["客户端"]
    Browser["浏览器<br/>http://localhost:8080"]
  end

  subgraph App["workOut 应用进程（CLI 启动）"]
    subgraph FE["前端静态资源"]
      React["React SPA<br/>登录/注册 + 三 Tab"]
      Guard["鉴权守卫<br/>无 Token → /login"]
    end
    subgraph BE["Spring Boot"]
      Static["Static Resource<br/>托管 build 产物"]
      AuthFilter["JWT Filter"]
      API["REST API<br/>/api/v1/*"]
      AuthSvc["AuthService"]
      Service["业务 Service"]
      Repo["Repository / DAO"]
    end
  end

  subgraph Data["数据层"]
    MySQL[("MySQL<br/>work_out_user / work_out_daily_record / work_out_profile")]
  end

  Browser -->|页面与静态资源| Static
  Static --> React
  React --> Guard
  Browser -->|JSON + Bearer Token| AuthFilter
  AuthFilter -->|/auth/* 放行| API
  AuthFilter -->|业务接口校验| API
  API --> AuthSvc
  API --> Service
  AuthSvc --> Repo
  Service --> Repo
  Repo --> MySQL
```

### 1.1 分层说明

| 层 | 职责 |
| --- | --- |
| 浏览器 / React | 登录注册页、三 Tab SPA、鉴权守卫、本地存 Token、颜色语义、触发下载 |
| Spring Boot 静态托管 | 托管前端 `build` 产物，统一入口端口 |
| JWT Filter | 解析 `Authorization: Bearer`；业务接口无/无效 Token → 401；`/api/v1/auth/**` 放行 |
| API | `/api/v1/...`，统一响应 `{ code, msg, data }`，携带 `requestId`、`timestamp` |
| AuthService | 注册（哈希密码）、登录发 Token |
| 业务 Service | 从 SecurityContext / Token 取 userId；按用户过滤查询与写入 |
| MySQL | `work_out_user`、`work_out_daily_record`、`work_out_profile` 持久化（Flyway history 表不改名前缀） |

后端包按模块分层（`com.workout`）：

| 包 | 边界 |
| --- | --- |
| `config/` | Security、JWT 配置、SPA 回退、全局异常处理 |
| `common/` | `ApiResponse` / `ApiRequest`、业务/未授权异常、健康检查 |
| `modules.auth.api` | Auth Controller 与 DTO；`CurrentUser` 只读 JWT |
| `modules.auth.application` | `AuthService` 编排 |
| `modules.auth.domain` | `AuthPrincipal` |
| `modules.auth.infrastructure` | `UserEntity`/`UserRepository`、`JwtService`、`JwtAuthFilter` |
| `modules.record.*` | 日记录 + xlsx 导出（api / application / domain / infrastructure） |
| `modules.profile.*` | 个人资料与身体历史（api / application / infrastructure） |
| `modules.share.*` | 分享创建与公开报告（api / application / infrastructure） |
| `bootstrap` | 非 test profile 演示种子（demo 账号） |

业务 `userId` 只从 JWT 进入应用服务，禁止信任客户端传入身份。

### 1.2 部署与启动形态

```mermaid
flowchart LR
  Dev["开发者"] -->|一条 CLI<br/>如 mvnw spring-boot:run| JVM["Spring Boot 进程"]
  JVM --> Static2["静态页 + API + JWT"]
  JVM --> DB[("MySQL")]
  User["用户"] -->|浏览器| Static2
```

---

## 2. 前端信息架构（模块视图）

```mermaid
flowchart TB
  SPA["React SPA"]
  AuthPages["登录 /login<br/>注册 /register"]
  Nav["底部导航"]
  Record["记录 Tab"]
  Calendar["日历 Tab"]
  Profile["我的 Tab"]

  SPA --> AuthPages
  SPA --> Nav
  Nav -->|未登录点击| AuthPages
  Nav -->|已登录| Record
  Nav -->|已登录| Calendar
  Nav -->|已登录| Profile

  Record --> ConsumeForm["消耗表单"]
  Record --> IntakeForm["摄入表单"]
  Calendar --> WeekBar["周视图"]
  Calendar --> DayList["日列表"]
  Calendar --> SharePage["分享二级页"]
  Calendar --> ExportBtn["导出 xlsx"]
  Profile --> ProfileForm["昵称 / 身高 / 体重"]
  Profile --> Curve["成长曲线"]
  Profile --> Logout["退出登录"]
```

---

## 3. 后端模块与数据对象

```mermaid
flowchart LR
  subgraph Controllers
    C0["AuthController"]
    C1["DailyRecordController"]
    C2["ProfileController"]
    C3["ShareReportController"]
    C4["PublicReportController"]
    C5["AdminAccountController"]
  end

  subgraph Services
    S0["AuthService"]
    S1["DailyRecordService"]
    S2["ProfileService"]
    S3["XlsxExportWriter"]
    S4["ShareReportService"]
    S5["DemoDataSeeder"]
    S6["AdminAccountService"]
  end

  subgraph Persistence
    T0[("work_out_user")]
    T1[("work_out_daily_record")]
    T2[("work_out_profile")]
    T3[("work_out_profile_history")]
    T4[("work_out_share_report")]
  end

  C0 --> S0
  C1 --> S1
  C1 --> S3
  C2 --> S2
  C3 --> S4
  C4 --> S4
  C5 --> S6
  S6 --> T0
  S6 --> T2
  S6 --> T1
  S6 --> T4
  S5 --> T0
  S0 --> T0
  S1 --> T1
  S3 --> T1
  S2 --> T2
  S2 --> T3
  S4 --> T4
  T1 -.->|user_id| T0
  T2 -.->|user_id| T0
```

| 对象 | 关键字段 |
| --- | --- |
| User | id, username, email(可空唯一), passwordHash, createdAt |
| DailyRecord | id, **userId**, type(`CONSUME`/`INTAKE`), content, recordedAt, createdAt |
| Profile | id, **userId**（唯一）, nickname, heightCm, weightKg, updatedAt |
| ProfileHistory | id, **userId**, changedAt, nickname, heightCm, weightKg |
| ShareReport | id, token, **userId**, rangeFrom, rangeTo, snapshotJson |

---

## 4. 接口一览（与架构对应）

| 能力 | 方法 | 路径 | 鉴权 |
| --- | --- | --- | --- |
| 注册 | POST | `/api/v1/auth/register` | 公开 |
| 登录 | POST | `/api/v1/auth/login` | 公开 |
| 邮箱登录 | POST | `/api/v1/auth/loginByEmail` | 公开 |
| 当前用户 | GET | `/api/v1/auth/me` | JWT |
| 发邮箱验证码 | POST | `/api/v1/auth/email/sendCode` | LOGIN 公开；BIND/UNBIND 须 JWT |
| 绑定邮箱 | POST | `/api/v1/auth/email/bind` | JWT |
| 解绑邮箱 | POST | `/api/v1/auth/email/unbind` | JWT |
| 新增记录 | POST | `/api/v1/dailyRecords` | JWT |
| 按日查询 | GET | `/api/v1/dailyRecords?date=yyyy-MM-dd` | JWT |
| 导出 xlsx | GET | `/api/v1/dailyRecords/exportCsv?date=`（或 yearMonth / from+to） | JWT |
| 创建分享 | POST | `/api/v1/shareReports`（筛选参数同上） | JWT；落库后异步 AI，不阻塞 |
| 本人分享列表 | GET | `/api/v1/shareReports` | JWT |
| 公开报告 | GET | `/api/v1/reports/{id}` | 公开；含 advice + adviceStatus；就绪建议为 Markdown |
| 查询资料 | GET | `/api/v1/profile` | JWT |
| 保存资料 | PUT | `/api/v1/profile` | JWT |
| CMS 账户列表 | GET | `/api/v1/admin/accounts` | ADMIN JWT |
| CMS 用户详情 | GET | `/api/v1/admin/accounts/{userId}` | ADMIN JWT |
| CMS 已有分享 | GET | `/api/v1/admin/reports` | ADMIN JWT |
| CMS API Key 列表 | GET | `/api/v1/admin/apiKeys` | ADMIN JWT |
| CMS 密钥库列表 | GET | `/api/v1/admin/apiKeys/pool` | ADMIN JWT；仅掩码 |
| CMS 密钥库新增 | POST | `/api/v1/admin/apiKeys/pool` | ADMIN JWT；入库后新注册可默认分配 |
| CMS 单用户改 Key | PUT | `/api/v1/admin/apiKeys/{userId}` | ADMIN JWT |
| CMS 批量改 Key | PUT | `/api/v1/admin/apiKeys/batch` | ADMIN JWT |
| CMS AI 调用 | GET | `/api/v1/admin/aiCalls?userId=&apiKeyId=` | ADMIN JWT |

---

## 5. 技术流程图

### 5.0 未登录跳转与登录发 Token

```mermaid
sequenceDiagram
  actor U as 用户
  participant SPA as React
  participant A as Auth API
  participant S as AuthService
  participant DB as MySQL

  U->>SPA: 点击 Tab / 保存 / 导出
  SPA->>SPA: 无 Token？
  alt 无 Token
    SPA-->>U: 跳转 /login?redirect=目标路径
    U->>SPA: 登录或注册
    SPA->>A: POST /api/v1/auth/login 或 register
    A->>S: 校验 / 建用户 / 签 JWT
    S->>DB: 读或写 work_out_user
    A-->>SPA: { token, userId, username }
    SPA->>SPA: 存 Token；跳转 redirect
  else 有 Token
    SPA->>SPA: 进入业务页并带 Bearer 调 API
  end
```

### 5.1 新增消耗 / 摄入记录

```mermaid
sequenceDiagram
  actor U as 用户
  participant P as 记录页 React
  participant F as JWT Filter
  participant A as API
  participant S as DailyRecordService
  participant DB as MySQL

  U->>P: 填写内容并保存
  P->>P: 前端校验
  alt 无 Token
    P-->>U: 跳转登录
  else 有 Token
    P->>F: POST /api/v1/dailyRecords + Bearer
    F->>A: 注入 userId
    A->>S: 业务校验 + 落库（绑定 userId）
    S->>DB: INSERT work_out_daily_record
    A-->>P: { code:200, data }
    P-->>U: 「已记录」
  end
```

### 5.2 日历按日回看

```mermaid
sequenceDiagram
  actor U as 用户
  participant C as 日历页 React
  participant A as API
  participant S as DailyRecordService
  participant DB as MySQL

  U->>C: 进入日历 / 切换选中日
  C->>A: GET /api/v1/dailyRecords?date=... + Bearer
  A->>S: 按 userId + 自然日查询
  S->>DB: SELECT ... WHERE user_id=? ORDER BY recorded_at ASC, id ASC
  A-->>C: { date, list }
  alt list 为空
    C-->>U: 「这一天还没有记录」
  else 有数据
    C-->>U: 正序渲染；CONSUME 绿 / INTAKE 红
  end
```

### 5.3 按筛选导出 xlsx

```mermaid
sequenceDiagram
  actor U as 用户
  participant C as 日历页 React
  participant A as API
  participant S as DailyRecordService
  participant W as XlsxExportWriter
  participant DB as MySQL

  U->>C: 点击「导出」
  C->>A: GET .../exportCsv?date=... + Bearer
  A->>S: 校验身高体重并按当前 userId 查区间
  S->>DB: 一次查事项 + 一次查身体历史
  S->>W: 双工作表：事项三列 + 曲线身高体重
  A-->>C: 文件流 workout-....xlsx
  C-->>U: 浏览器下载
```

分享走日历二级页 `/calendar/share`，再 `POST /api/v1/shareReports`，不在日历主页内联贴链接。

### 5.4 保存个人资料

```mermaid
sequenceDiagram
  actor U as 用户
  participant M as 我的页 React
  participant A as API
  participant S as ProfileService
  participant DB as MySQL

  U->>M: 进入「我的」
  M->>A: GET /api/v1/profile + Bearer
  A->>S: 按 userId 读资料
  S->>DB: SELECT
  DB-->>M: 回填

  U->>M: 修改后保存
  M->>A: PUT /api/v1/profile + Bearer
  A->>S: upsert WHERE user_id=?
  S->>DB: INSERT/UPDATE
  A-->>M: 保存后资料
  M-->>U: 「已保存」
```

### 5.5 CLI 启动与访问闭环

```mermaid
flowchart TD
  A["项目根目录执行 CLI 启动"] --> B{"MySQL 可用？"}
  B -->|否| B1["启动失败 / 接口 500"]
  B -->|是| C["Spring Boot 启动完成"]
  C --> D["静态资源 + JWT + /api/v1"]
  D --> E["浏览器打开 localhost:8080"]
  E --> F["看到导航壳"]
  F --> G["点 Tab → 登录/注册"]
  G --> H["记账 → 日历 → 导出 → 资料"]
```

### 5.6 端到端业务主路径（产品视角）

```mermaid
flowchart LR
  Start(["打开应用"]) --> Shell["见导航壳"]
  Shell --> Auth["登录 / 注册"]
  Auth --> Record["记录消耗/摄入"]
  Record --> Calendar["日历选日回看"]
  Calendar --> Export["按筛选导出 xlsx"]
  Calendar --> Share["分享二级页 H5"]
  Calendar --> Profile["我的：身高体重"]
  Profile --> Done(["MVP 闭环完成"])
  Export --> Done
  Share --> Done
```

---

## 6. 关键设计约束（实现时遵守）

| 约束 | 说明 |
| --- | --- |
| 多账号隔离 | 所有业务读写必须带当前 `userId`；禁止客户端传 userId 覆盖 |
| 鉴权 | JWT；公开 `/api/v1/auth/register|login`、`/api/v1/health`、`/api/v1/reports/**`；业务 401 |
| 演示种子 | 非 test profile 启动时若无 `demo` 用户则写入约 ±90 天数据；`saveAll` 批量，禁止 N+1 |
| 密码 | 仅存哈希；错误登录统一文案 |
| 时区 | 默认 `Asia/Shanghai`；自然日按本地时区切分 |
| 本期写策略 | 记录只新增，不改不删 |
| 颜色语义 | 消耗绿 `#16A34A`，摄入红 `#DC2626` |
| 响应格式 | `{ code, msg, data }`；校验失败 400；未鉴权 401 |
| 部署 | 前后端不分离，不以微服务拆分为验收目标 |

---

## 7. 与产品 / 功能文档的对应

| 本文章节 | 对应文档 |
| --- | --- |
| §1～§3 架构（含 JWT） | 产品文档 §3、§7；功能文档 §3、§8、§9 |
| §4 接口 | 功能文档 §8 |
| §5.0 登录跳转 | 产品文档 §4.0；功能文档 §3 |
| §5.1 记账流程 | 功能文档 §4；产品文档 §4.1 |
| §5.2～5.3 日历与导出 | 功能文档 §5；产品文档 §4.2 |
| §5.4 资料 | 功能文档 §6；产品文档 §4.3 |
| §5.5 启动 | 功能文档 §9 |
| CMS | 功能文档 §6.4、§8.9～8.11；产品文档 §4.4 |
| AI 建议 / 向量化 | [workOut-AI方案.md](./workOut-AI方案.md)；[workOut-上下文工程.md](./workOut-上下文工程.md) |
