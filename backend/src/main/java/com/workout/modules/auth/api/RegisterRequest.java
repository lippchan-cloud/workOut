package com.workout.modules.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求体（API 边界 DTO）。
 * 仅承载客户端入参与校验注解，不进入持久化层。
 */
public class RegisterRequest {

    @NotBlank(message = "请填写合法用户名")
    @Size(min = 3, max = 32, message = "请填写合法用户名")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "请填写合法用户名")
    private String username;

    @NotBlank(message = "密码长度需为 6～64 位")
    @Size(min = 6, max = 64, message = "密码长度需为 6～64 位")
    private String password;

    /**
     * 读取用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 写入用户名。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 读取明文密码（仅传输层短暂存在，落库前必须哈希）。
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
