package com.workout.modules.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 解绑邮箱请求（API 边界 DTO）。
 * 验证码必须为 4 位数字，发往当前已绑定邮箱。
 */
public class UnbindEmailRequest {

    @NotBlank(message = "请填写验证码")
    @Pattern(regexp = "^\\d{4}$", message = "验证码为4位数字")
    private String code;

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
