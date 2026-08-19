package com.workout.support;

import com.workout.modules.auth.application.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 测试用验证码捕获器（测试支撑）。
 * 不发真实邮件，把最近一次明文码留在内存供集成测试读取。
 */
@Component
@Primary
@Profile("test")
public class CapturingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(CapturingEmailSender.class);

    private volatile String lastEmail;
    private volatile String lastPurpose;
    private volatile String lastCode;

    /**
     * 捕获最近一次验证码。
     */
    @Override
    public void sendVerificationCode(String email, String purpose, String code) {
        log.info("[邮箱验证码] capturing send start purpose={}, email={}", purpose, email);
        this.lastEmail = email;
        this.lastPurpose = purpose;
        this.lastCode = code;
        log.info("[邮箱验证码] capturing send done purpose={}, email={}", purpose, email);
    }

    /**
     * 最近一次明文验证码。
     */
    public String getLastCode() {
        return lastCode;
    }

    /**
     * 最近一次目标邮箱。
     */
    public String getLastEmail() {
        return lastEmail;
    }

    /**
     * 最近一次用途。
     */
    public String getLastPurpose() {
        return lastPurpose;
    }
}
