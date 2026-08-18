package com.workout.modules.profile.api;

import com.workout.modules.profile.infrastructure.ProfileEntity;
import java.math.BigDecimal;

/**
 * 资料响应 DTO（API 边界）。
 */
public class ProfileResponse {

    private final String nickname;
    private final BigDecimal heightCm;
    private final BigDecimal weightKg;

    /**
     * 构造响应。
     */
    public ProfileResponse(String nickname, BigDecimal heightCm, BigDecimal weightKg) {
        this.nickname = nickname;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    /**
     * 空资料。
     */
    public static ProfileResponse empty() {
        return new ProfileResponse(null, null, null);
    }

    /**
     * 从持久化实体转换到 API 边界。
     */
    public static ProfileResponse from(ProfileEntity entity) {
        return new ProfileResponse(entity.getNickname(), entity.getHeightCm(), entity.getWeightKg());
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
