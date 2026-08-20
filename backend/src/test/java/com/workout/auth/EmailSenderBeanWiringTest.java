package com.workout.auth;

import com.workout.modules.auth.application.EmailSender;
import com.workout.modules.auth.infrastructure.DefaultEmailSender;
import com.workout.modules.auth.infrastructure.SmtpEmailSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证有 JavaMailSender 时 DefaultEmailSender 走 SMTP，而非仅打日志。
 */
class EmailSenderBeanWiringTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DefaultEmailSender.class);

    /**
     * 存在 JavaMailSender 时，发信应调用 mailSender.send。
     */
    @Test
    void withMailSender_routesToSmtpSend() {
        RecordingMailSender mailSender = new RecordingMailSender();

        runner.withBean(JavaMailSender.class, () -> mailSender)
                .withPropertyValues("workout.mail.from=lippcloud@163.com")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(EmailSender.class);
                    assertThat(ctx.getBean(EmailSender.class)).isInstanceOf(DefaultEmailSender.class);
                    // 触发发信
                    ctx.getBean(EmailSender.class).sendVerificationCode("user@example.com", "BIND", "1234");
                    assertThat(mailSender.simpleMessages).hasSize(1);
                    SimpleMailMessage sent = mailSender.simpleMessages.get(0);
                    assertThat(sent.getTo()).containsExactly("user@example.com");
                    assertThat(sent.getFrom()).isEqualTo("lippcloud@163.com");
                    // 收件人看到中文提示词，而不是只有 4 位数字
                    assertThat(sent.getSubject()).isEqualTo("workOut 绑定邮箱验证码");
                    assertThat(sent.getText()).contains("绑定邮箱");
                    assertThat(sent.getText()).contains("请使用以下 4 位验证码完成验证");
                    assertThat(sent.getText()).contains("验证码：1234");
                    assertThat(sent.getText()).contains("10 分钟内有效");
                    assertThat(sent.getText()).contains("如非本人操作，请忽略本邮件");
                });
    }

    /**
     * 登录用途邮件同样使用中文主题与提示正文（不连真实 SMTP）。
     */
    @Test
    void smtpCopy_usesChinesePromptForLogin() {
        RecordingMailSender mailSender = new RecordingMailSender();
        // 直接组装 SMTP 实现以断言提示词
        new SmtpEmailSender(mailSender, "from@example.com")
                .sendVerificationCode("user@example.com", "LOGIN", "5678");
        assertThat(mailSender.simpleMessages).hasSize(1);
        assertThat(mailSender.simpleMessages.get(0).getSubject()).isEqualTo("workOut 登录验证码");
        assertThat(mailSender.simpleMessages.get(0).getText()).contains("邮箱登录");
        assertThat(mailSender.simpleMessages.get(0).getText()).contains("验证码：5678");
    }

    /**
     * 无 JavaMailSender 时回落日志路径且不抛错。
     */
    @Test
    void withoutMailSender_routesToLogging() {
        runner.withPropertyValues("workout.mail.from=")
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(JavaMailSender.class);
                    assertThat(ctx).hasSingleBean(EmailSender.class);
                    // 无 SMTP 时只打日志，不应抛异常
                    ctx.getBean(EmailSender.class).sendVerificationCode("user@example.com", "LOGIN", "5678");
                });
    }

    /**
     * 配置 spring.mail.host 后自动配置会创建 JavaMailSender，供路由选用。
     */
    @Test
    void mailAutoConfig_exposesJavaMailSender() {
        new ApplicationContextRunner()
                .withUserConfiguration(DefaultEmailSender.class)
                .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
                .withPropertyValues(
                        "spring.mail.host=smtp.163.com",
                        "spring.mail.port=465",
                        "spring.mail.username=lippcloud@163.com",
                        "spring.mail.password=dummy",
                        "workout.mail.from=lippcloud@163.com")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JavaMailSender.class);
                    assertThat(ctx.getBean(EmailSender.class)).isInstanceOf(DefaultEmailSender.class);
                });
    }

    /**
     * 测试用邮件发送器：只记录 SimpleMailMessage，不连真实 SMTP。
     */
    private static final class RecordingMailSender implements JavaMailSender {

        private final List<SimpleMailMessage> simpleMessages = new ArrayList<>();

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            simpleMessages.add(simpleMessage);
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            for (SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            throw new UnsupportedOperationException("mime not used in this test");
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            throw new UnsupportedOperationException("mime not used in this test");
        }
    }
}
