package com.workout.modules.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 邮箱验证码登录请求（API 边界 DTO）。
 * 失败文案不得区分邮箱是否已注册。
 */
public class EmailLoginRequest {

    @NotBlank(message = "请填写邮箱")
    @Email(message = "请填写合法邮箱")
    private String email;

    @NotBlank(message = "请填写验证码")
    @Pattern(regexp = "^\\d{4}$", message = "验证码为4位数字")
    private String code;

    /**
     * 已绑定邮箱。
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
     * 4 位验证码。
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置验证码。
     */
    public void setCode(String code) {
        this.code = code;
    }
}
