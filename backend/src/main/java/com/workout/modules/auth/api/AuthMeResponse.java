package com.workout.modules.auth.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.workout.modules.auth.domain.UserRole;

/**
 * 当前会话用户信息（API 响应 DTO）。
 * 不含密码哈希与 Token。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthMeResponse {

    private final Long userId;
    private final String username;
    private final UserRole role;
    private final String email;

    /**
     * 构造当前用户快照。
     *
     * @param userId   主键
     * @param username 登录名
     * @param role     角色
     * @param email    绑定邮箱，未绑定为 null
     */
    public AuthMeResponse(Long userId, String username, UserRole role, String email) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
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
     * 角色。
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * 绑定邮箱；未绑定为 null。
     */
    public String getEmail() {
        return email;
    }
}
