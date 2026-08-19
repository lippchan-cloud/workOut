
package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 默认验证码投递路由（基础设施层）。
 * 运行时若存在 {@link JavaMailSender} 则走 SMTP，否则回落日志；避免
 * {@code @ConditionalOnBean} 在装配阶段过早失败。测试由 CapturingEmailSender {@code @Primary} 覆盖。
 */
@Component
public class DefaultEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DefaultEmailSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;
    private final LoggingEmailSender loggingFallback = new LoggingEmailSender();

    /**
     * 注入可选的 JavaMailSender 与发件人。
     *
     * @param mailSenderProvider Spring 可选邮件发送器
     * @param fromAddress        发件人（须与 163 授权账号一致）
     */
    public DefaultEmailSender(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${workout.mail.from:${spring.mail.username:}}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    /**
     * 有 SMTP 则真实发信，否则写日志联调。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        // 运行时探测，不依赖 ConditionalOnBean 装配顺序
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            log.info("[邮箱验证码] route smtp purpose={}, email={}", purpose, mask(email));
            // 委托 SMTP 实现投递
            new SmtpEmailSender(mailSender, resolveFrom()).sendVerificationCode(email, purpose, code);
            return;
        }
        log.info("[邮箱验证码] route logging purpose={}, email={}", purpose, mask(email));
        // 无 JavaMailSender 时回落日志
        loggingFallback.sendVerificationCode(email, purpose, code);
    }

    /**
     * 解析发件人；空则无法满足 163 From 校验，仍交由 Smtp 层报错。
     */
    private String resolveFrom() {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress.trim();
        }
        return fromAddress;
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
