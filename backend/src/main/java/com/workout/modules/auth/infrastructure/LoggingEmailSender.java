package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认验证码投递：写 INFO 日志（基础设施层）。
 * 未配置 SMTP 时供本地/演示使用；测试 profile 由 CapturingEmailSender @Primary 覆盖注入。
 */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    /**
     * 记录验证码到日志，便于无 SMTP 环境完成绑定与登录。
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        log.info("[邮箱验证码] sendVerificationCode start purpose={}, email={}", purpose, mask(email));
        log.info("[邮箱验证码] sendVerificationCode delivered purpose={}, email={}, code={}", purpose, mask(email), code);
    }

    /**
     * 脱敏邮箱：保留首字符与域名。
     */
    private String mask(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
