package com.workout.modules.auth.application;

/**
 * 邮箱验证码投递（应用层端口）。
 * 实现可走 SMTP 或日志；禁止把明文码写入 HTTP 响应。
 */
public interface EmailSender {

    /**
     * 向目标邮箱发送 4 位数字验证码。
     *
     * @param email   规范化邮箱
     * @param purpose 用途标签（BIND/UNBIND/LOGIN）
     * @param code    明文 4 位码，仅用于投递
     */
    void sendVerificationCode(String email, String purpose, String code);
}
