package com.workout.modules.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 绑定邮箱请求（API 边界 DTO）。
 * 验证码必须为 4 位数字。
 */
public class BindEmailRequest {

    @NotBlank(message = "请填写邮箱")
    @Email(message = "请填写合法邮箱")
    private String email;

    @NotBlank(message = "请填写验证码")
    @Pattern(regexp = "^\\d{4}$", message = "验证码为4位数字")
    private String code;

    /**
     * 待绑定邮箱。
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
