# workOut 邮箱 SMTP 配置

| 项 | 内容 |
| --- | --- |
| 产品名称 | workOut |
| 文档类型 | 邮箱 SMTP |
| 文档版本 | v1.0 |
| 日期 | 2026-08-19 |
| 依据 | 163 邮箱客户端 SMTP（授权码） |
| 文档用途 | 开发、联调、Docker 启动时发送 C 端邮箱验证码 |

---

## 1. 用途

C 端 **绑定 / 解绑 / 邮箱登录** 的 **4 位数字验证码** 发信。后端经 `DefaultEmailSender` 路由：存在 `JavaMailSender`（已配 `spring.mail.host`）时委托 `SmtpEmailSender`；否则回落 `LoggingEmailSender`（前缀 `[邮箱验证码]`）。日志关键字：`route smtp` / `smtp sendVerificationCode done`。

---

## 2. 账号与服务器

| 项 | 值 |
| --- | --- |
| 发信账号 | `lippcloud@163.com` |
| SMTP 主机 | `smtp.163.com` |
| 授权码 | `JYQ8CqSJmZ7GEuAD`（**不是** 163 网页/客户端登录密码） |
| 推荐端口 | **465 + SSL**（本文默认） |
| 备选端口 | 587 + STARTTLS（若网络限制 465 时可改） |

---

## 3. Spring Boot 配置（默认：465 SSL）

依赖：`spring-boot-starter-mail`（Spring Boot 3.3）。`application.yml` / `application-docker.yml` 示例：

```yaml
spring:
  mail:
    host: ${WORKOUT_MAIL_HOST:smtp.163.com}
    port: ${WORKOUT_MAIL_PORT:465}
    username: ${WORKOUT_MAIL_USERNAME:lippcloud@163.com}
    password: ${WORKOUT_MAIL_PASSWORD:JYQ8CqSJmZ7GEuAD}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
            trust: smtp.163.com
          connectiontimeout: 10000
          timeout: 10000
          writetimeout: 10000

workout:
  mail:
    from: ${WORKOUT_MAIL_FROM:lippcloud@163.com}
```

`workout.mail.from` 须与授权账号一致（163 对发件人校验较严）。

### 3.1 备选：587 STARTTLS

若改用 587，将端口与属性改为：

```yaml
spring:
  mail:
    port: 587
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            enable: false
```

---

## 4. 环境变量覆盖

与数据库文档相同：yml 可写私有仓默认值，也可用环境变量覆盖。

| 变量 | 说明 |
| --- | --- |
| `WORKOUT_MAIL_HOST` | SMTP 主机；清空/去掉 host 则不创建 `JavaMailSender`，回落日志发信 |
| `WORKOUT_MAIL_PORT` | 端口，默认 `465` |
| `WORKOUT_MAIL_USERNAME` | SMTP 用户名（邮箱） |
| `WORKOUT_MAIL_PASSWORD` | **授权码**（非登录密码） |
| `WORKOUT_MAIL_FROM` | 发件人地址，默认与用户名相同 |

Docker 示例：

```bash
docker run --rm -p 8080:8080 \
  -e WORKOUT_MAIL_PASSWORD='你的授权码' \
  workout:local
```

---

## 5. 安全说明

- 授权码只用于 SMTP 客户端登录，**不是** 163 账号登录密码；泄露后应在邮箱设置里重置授权码。
- 默认值仅适合 **私有仓库**（与 DB 密码写入 `application.yml` 同一风格）。**不要**推到公开仓或粘贴到公开聊天/工单。
- 应用日志对邮箱脱敏；SMTP 实现 **不** 把授权码或验证码打进成功日志（日志回落模式为联调会打印验证码）。

---

## 6. 如何验证

1. 启动后端后，在「我的 → 账号安全」发起绑定（或调用 `POST /api/v1/auth/email/sendCode`）。
2. 日志中搜索 `[邮箱验证码]`：应出现 `route smtp` 与 `smtp sendVerificationCode start/done`（若仍是 `route logging` / `logging ... delivered`，说明未加载 `JavaMailSender`，请确认已配 `spring.mail.host` 并重启）。
3. 目标邮箱应收到主题为「workOut 验证码」的邮件，正文含 4 位数字码；也请检查垃圾箱。用该码完成绑定/登录/解绑。

集成测试使用 `CapturingEmailSender`（`@Primary` + `test` profile），不发真实邮件。
