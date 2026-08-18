package com.workout.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体（API 边界 DTO）。
 * 仅承载用户名与明文密码；失败时不得据此区分用户是否存在。
 */
public class LoginRequest {

    @NotBlank(message = "请填写用户名")
    private String username;

    @NotBlank(message = "请填写密码")
    private String password;

    /**
     * 读取登录用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 写入登录用户名。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 读取明文密码（仅传输层短暂存在）。
     */
    public String getPassword() {
        return password;
    }

    /**
     * 写入明文密码。
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
