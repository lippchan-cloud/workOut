package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 基于 SMTP 的验证码投递（基础设施层）。
 * 由 {@link DefaultEmailSender} 在 JavaMailSender 就绪后委托调用；禁止把授权码写入日志。
 */
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
    public SmtpEmailSender(JavaMailSender mailSender, String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * 通过 SMTP 向目标邮箱投递带中文提示词的 4 位验证码邮件。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码，仅用于邮件正文，禁止写入 INFO
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        long startMs = System.currentTimeMillis();
        // 关键入口：脱敏邮箱与用途，禁止打印授权码或验证码
        log.info("[邮箱验证码] smtp sendVerificationCode start purpose={}, email={}, from={}, subject={}",
                purpose, mask(email), mask(fromAddress), VerificationEmailCopy.subject(purpose));
        SimpleMailMessage message = new SimpleMailMessage();
        // 163 要求 From 与登录账号一致
        message.setFrom(fromAddress);
        message.setTo(email);
        // 主题与正文使用中文提示词，避免邮件里只剩 4 位数字
        message.setSubject(VerificationEmailCopy.subject(purpose));
        message.setText(VerificationEmailCopy.body(purpose, code));
        try {
            // 经 SMTP 投递验证码邮件
            mailSender.send(message);
        } catch (RuntimeException ex) {
            log.error("[邮箱验证码] smtp sendVerificationCode failed purpose={}, email={}, msg={}",
                    purpose, mask(email), ex.getMessage());
            throw ex;
        }
        // 关键结果：投递成功与耗时（不打印验证码）
        log.info("[邮箱验证码] smtp sendVerificationCode done purpose={}, email={}, elapsedMs={}",
                purpose, mask(email), System.currentTimeMillis() - startMs);
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
