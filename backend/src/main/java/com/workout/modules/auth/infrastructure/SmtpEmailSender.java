package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 基于 SMTP 的验证码投递（基础设施层）。
 * 在存在 {@link JavaMailSender}（已配置 {@code spring.mail.host}）时启用；禁止把授权码写入日志。
 */
@Component
@ConditionalOnBean(JavaMailSender.class)
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    /**
     * 注入 Spring Boot 自动配置的邮件发送器与发件人地址。
     *
     * @param mailSender  JavaMail 发送器
     * @param fromAddress 发件人（须与 163 授权账号一致）
     */
    public SmtpEmailSender(
            JavaMailSender mailSender,
            @Value("${workout.mail.from:${spring.mail.username}}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * 通过 SMTP 向目标邮箱投递 4 位数字验证码。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码，仅用于邮件正文
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        long startMs = System.currentTimeMillis();
        // 关键入口：脱敏邮箱与用途，禁止打印授权码或完整明文码以外的密钥
        log.info("[邮箱验证码] smtp sendVerificationCode start purpose={}, email={}, from={}",
                purpose, mask(email), mask(fromAddress));
        SimpleMailMessage message = new SimpleMailMessage();
        // 163 要求 From 与登录账号一致
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("workOut 验证码");
        message.setText(buildBody(purpose, code));
        try {
            // 经 SMTP 投递验证码邮件
            mailSender.send(message);
        } catch (RuntimeException ex) {
            log.error("[邮箱验证码] smtp sendVerificationCode failed purpose={}, email={}, msg={}",
                    purpose, mask(email), ex.getMessage());
            throw ex;
        }
        // 关键结果：投递成功与耗时（不打印验证码，避免日志扩散）
        log.info("[邮箱验证码] smtp sendVerificationCode done purpose={}, email={}, elapsedMs={}",
                purpose, mask(email), System.currentTimeMillis() - startMs);
    }

    /**
     * 组装邮件正文（含明文验证码，仅出现在邮件中）。
     */
    private String buildBody(String purpose, String code) {
        return "您的 workOut 验证码是 " + code + "，10 分钟内有效。用途：" + purpose + "。如非本人操作请忽略。";
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
