package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认验证码投递：写日志（基础设施层）。
 * 由 {@link DefaultEmailSender} 在缺少 JavaMailSender 时回落调用。
 * INFO 只打脱敏邮箱与中文主题，明文验证码仅 DEBUG；测试 profile 由 CapturingEmailSender {@code @Primary} 覆盖。
 */
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    /**
     * 记录验证码邮件主题到 INFO（不含明文码），便于无 SMTP 环境确认提示词。
     * 明文正文仅 DEBUG；生产应配置 spring.mail.host 走 SMTP。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码，仅用于组装正文，禁止写入 INFO
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        // 关键入口：无 SMTP 时回落日志投递；邮箱脱敏，禁止打印授权码或完整验证码
        log.info("[邮箱验证码] logging sendVerificationCode start purpose={}, email={}", purpose, mask(email));
        // 记录将投递的中文主题，便于确认提示词；明文码只进 DEBUG
        String subject = VerificationEmailCopy.subject(purpose);
        log.info("[邮箱验证码] logging sendVerificationCode delivered purpose={}, email={}, subject={}",
                purpose, mask(email), subject);
        log.debug("[邮箱验证码] logging mail body purpose={}, email={}, body={}",
                purpose, mask(email), VerificationEmailCopy.body(purpose, code));
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
