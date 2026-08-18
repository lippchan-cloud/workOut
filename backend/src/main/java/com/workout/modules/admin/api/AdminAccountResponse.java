package com.workout.modules.admin.api;

import com.workout.modules.auth.domain.UserRole;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * CMS 账户列表项（API 边界）。
 * 仅含用户名、创建时间、角色与资料可见字段；禁止携带密码或密码哈希。
 */
public class AdminAccountResponse {

    private final Long userId;
    private final String username;
    private final Instant createdAt;
    private final UserRole role;
    private final String nickname;
    private final BigDecimal heightCm;
    private final BigDecimal weightKg;

    /**
     * 构造不含密钥字段的账户视图。
     */
    public AdminAccountResponse(
            Long userId,
            String username,
            Instant createdAt,
            UserRole role,
            String nickname,
            BigDecimal heightCm,
            BigDecimal weightKg) {
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.role = role;
        this.nickname = nickname;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    /**
     * 用户主键。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 登录用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 账户创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 角色。
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * 昵称；无资料时为 null。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 身高厘米；无资料时为 null。
     */
    public BigDecimal getHeightCm() {
        return heightCm;
    }

    /**
     * 体重千克；无资料时为 null。
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }
}
