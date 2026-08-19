package com.workout.auth;

import com.workout.modules.auth.application.EmailSender;
import com.workout.modules.auth.infrastructure.DefaultEmailSender;
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
                    assertThat(mailSender.simpleMessages.get(0).getTo()).containsExactly("user@example.com");
                    assertThat(mailSender.simpleMessages.get(0).getFrom()).isEqualTo("lippcloud@163.com");
                });
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
