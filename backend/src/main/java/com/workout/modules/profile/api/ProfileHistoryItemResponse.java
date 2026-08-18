package com.workout.modules.profile.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 单条身体资料历史快照（API 边界 DTO）。
 */
public class ProfileHistoryItemResponse {

    private final Instant changedAt;
    private final String nickname;
    private final BigDecimal heightCm;
    private final BigDecimal weightKg;

    /**
     * 构造快照响应。
     */
    public ProfileHistoryItemResponse(Instant changedAt, String nickname, BigDecimal heightCm, BigDecimal weightKg) {
        this.changedAt = changedAt;
        this.nickname = nickname;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    /**
     * 快照生效时间。
     */
    public Instant getChangedAt() {
        return changedAt;
    }

    /**
     * 昵称。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 身高厘米。
     */
    public BigDecimal getHeightCm() {
        return heightCm;
    }

    /**
     * 体重千克。
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }
}
