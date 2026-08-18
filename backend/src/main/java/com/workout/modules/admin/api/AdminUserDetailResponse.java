package com.workout.modules.admin.api;

import com.workout.modules.auth.domain.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * CMS 用户详情（API 边界）。
 * 含资料、最近记录摘要与该用户已有分享链接；禁止携带密码哈希。
 */
public class AdminUserDetailResponse {

    private final Long userId;
    private final String username;
    private final Instant createdAt;
    private final UserRole role;
    private final String nickname;
    private final BigDecimal heightCm;
    private final BigDecimal weightKg;
    private final long recordCount;
    private final List<AdminRecentRecordResponse> recentRecords;
    private final List<AdminShareListItemResponse> shares;

    /**
     * 构造运营只读用户详情。
     */
    public AdminUserDetailResponse(
            Long userId,
            String username,
            Instant createdAt,
            UserRole role,
            String nickname,
            BigDecimal heightCm,
            BigDecimal weightKg,
            long recordCount,
            List<AdminRecentRecordResponse> recentRecords,
            List<AdminShareListItemResponse> shares) {
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.role = role;
        this.nickname = nickname;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.recordCount = recordCount;
        this.recentRecords = recentRecords;
        this.shares = shares;
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

    /**
     * 未删除日记录条数。
     */
    public long getRecordCount() {
        return recordCount;
    }

    /**
     * 最近若干条记录。
     */
    public List<AdminRecentRecordResponse> getRecentRecords() {
        return recentRecords;
    }

    /**
     * 该用户已有分享（只读）。
     */
    public List<AdminShareListItemResponse> getShares() {
        return shares;
    }
}
