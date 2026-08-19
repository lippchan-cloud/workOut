package com.workout.modules.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证码请求（API 边界 DTO）。
 * purpose 为 BIND / UNBIND / LOGIN。
 */
public class SendEmailCodeRequest {

    @NotBlank(message = "请填写邮箱")
    @Email(message = "请填写合法邮箱")
    private String email;

    @NotBlank(message = "请填写验证码用途")
    private String purpose;

    /**
     * 目标邮箱。
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱。
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 用途。
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * 设置用途。
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
