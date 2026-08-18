package com.workout.modules.auth.api;

import com.workout.modules.auth.domain.UserRole;

/**
 * 登录/注册成功后的鉴权载荷（API 响应 DTO）。
 * 仅返回给前端的必要字段，不包含密码哈希。
 */
public class AuthTokenResponse {

    private final String token;
    private final Long userId;
    private final String username;
    private final UserRole role;

    /**
     * 构造鉴权响应。
     *
     * @param token    JWT
     * @param userId   用户主键
     * @param username 用户名
     * @param role     USER 或 ADMIN
     */
    public AuthTokenResponse(String token, Long userId, String username, UserRole role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
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

    /**
     * 角色，供前端隐藏 CMS 入口。
     */
    public UserRole getRole() {
        return role;
    }
}
