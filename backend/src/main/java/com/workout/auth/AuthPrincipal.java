package com.workout.auth;

/**
 * 已认证主体（安全上下文）。
 * 仅承载从 JWT 解析出的 userId 与 username，禁止包含密码或完整 Token。
 */
public class AuthPrincipal {

    private final Long userId;
    private final String username;

    /**
     * 构造已认证主体。
     *
     * @param userId   用户主键
     * @param username 用户名
     */
    public AuthPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * 当前用户主键（来自 JWT claim，不信任客户端 body）。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 当前用户名。
     */
    public String getUsername() {
        return username;
    }
}
