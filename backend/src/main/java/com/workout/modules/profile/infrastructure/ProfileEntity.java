package com.workout.modules.profile.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 用户资料持久化实体（持久化层）。
 * 对应表 work_out_profile；每个 userId 至多一行。
 */
@Entity
@Table(name = "work_out_profile")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(length = 32)
    private String nickname;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * 主键。
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键。
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 所属用户。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置所属用户。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 昵称。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置昵称。
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 身高厘米。
     */
    public BigDecimal getHeightCm() {
        return heightCm;
    }

    /**
     * 设置身高。
     */
    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    /**
     * 体重千克。
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }

    /**
     * 设置体重。
     */
    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    /**
     * 最近更新时间。
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
