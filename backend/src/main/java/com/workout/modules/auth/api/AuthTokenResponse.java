package com.workout.modules.auth.api;

/**
 * 登录/注册成功后的鉴权载荷（API 响应 DTO）。
 * 仅返回给前端的必要字段，不包含密码哈希。
 */
public class AuthTokenResponse {

    private final String token;
    private final Long userId;
    private final String username;

    /**
     * 构造鉴权响应。
     */
    public AuthTokenResponse(String token, Long userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    /**
     * JWT 字符串。
     */
    public String getToken() {
        return token;
    }

    /**
     * 用户主键。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 用户名。
     */
    public String getUsername() {
        return username;
    }
}
