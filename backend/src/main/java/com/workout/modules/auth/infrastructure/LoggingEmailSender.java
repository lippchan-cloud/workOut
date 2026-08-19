package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认验证码投递：写 INFO 日志（基础设施层）。
 * 由 {@link com.workout.config.EmailSenderConfiguration} 在缺少其它 {@link EmailSender} 时注册。
 * 测试 profile 由 CapturingEmailSender {@code @Primary} 覆盖注入。
 */
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    /**
     * 记录验证码到日志，便于无 SMTP 环境完成绑定与登录。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码，仅用于投递/联调
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        // 关键入口：无 SMTP 时回落日志投递
        log.info("[邮箱验证码] logging sendVerificationCode start purpose={}, email={}", purpose, mask(email));
        // 联调可读明文码；生产应配置 spring.mail.host 走 SMTP
        log.info("[邮箱验证码] logging sendVerificationCode delivered purpose={}, email={}, code={}",
                purpose, mask(email), code);
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
