# workOut 数据库连接信息

| 项 | 内容 |
| --- | --- |
| 产品名称 | workOut |
| 文档类型 | 数据库连接 |
| 文档版本 | v1.0 |
| 日期 | 2026-08-18 |
| 依据 | SQLPub 实例控制台 |
| 文档用途 | 开发、联调、CLI 启动时连接 MySQL |

---

## 1. 实例信息

| 项 | 值 |
| --- | --- |
| 公网连接地址 | `mysql5.sqlpub.com:3310` |
| Host | `mysql5.sqlpub.com` |
| Port | `3310` |
| 数据库名称 | `inv_doc` |
| 数据库账号 | `user_lipp` |
| 密码 | `pgGSlfNr1pSJbcq1` |
| 数据库版本 | MySQL 8.0.40 |
| 数据库状态 | 正常 |
| 节点区域 | 中国 |
| 资源类型 | 免费版 |
| 创建时间 | 2025-11-02 12:34:15 |
| 到期时间 | 2099-12-31 23:59:59 |

---

## 2. 连接串

### 2.1 JDBC（Spring Boot）

```text
jdbc:mysql://mysql5.sqlpub.com:3310/inv_doc?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
```

`application.yml` 示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql5.sqlpub.com:3310/inv_doc?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: user_lipp
    password: pgGSlfNr1pSJbcq1
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2.2 MySQL URI

```text
mysql://user_lipp:pgGSlfNr1pSJbcq1@mysql5.sqlpub.com:3310/inv_doc
```

### 2.3 命令行

```bash
mysql -h mysql5.sqlpub.com -P 3310 -u user_lipp -p inv_doc
```

执行后按提示输入密码：`pgGSlfNr1pSJbcq1`。

---

## 3. 使用说明

- 本实例为公网免费节点，仅用于 workOut 开发与联调。
- 连接失败时先确认实例状态为「正常」，再检查本机网络是否能访问 `mysql5.sqlpub.com:3310`。
