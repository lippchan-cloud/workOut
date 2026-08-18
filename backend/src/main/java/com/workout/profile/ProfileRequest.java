package com.workout.profile;

import java.math.BigDecimal;

/**
 * 保存资料请求（API 边界 DTO）。
 */
public class ProfileRequest {

    private String nickname;
    private BigDecimal heightCm;
    private BigDecimal weightKg;

    /**
     * 读取昵称。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 写入昵称。
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 读取身高。
     */
    public BigDecimal getHeightCm() {
        return heightCm;
    }

    /**
     * 写入身高。
     */
    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    /**
     * 读取体重。
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }

    /**
     * 写入体重。
     */
    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }
}
