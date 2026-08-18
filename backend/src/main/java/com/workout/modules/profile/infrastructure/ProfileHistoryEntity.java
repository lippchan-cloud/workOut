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
 * 身体资料变更历史持久化实体（持久化层）。
 * 对应表 work_out_profile_history；存变更后快照，不得进入领域层参与记账规则。
 */
@Entity
@Table(name = "work_out_profile_history")
public class ProfileHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(length = 32)
    private String nickname;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

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
     * 该快照生效时刻（写入时间）。
     */
    public Instant getChangedAt() {
        return changedAt;
    }

    /**
     * 设置生效时刻。
     */
    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    /**
     * 变更后昵称。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置昵称快照。
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 变更后身高厘米。
     */
    public BigDecimal getHeightCm() {
        return heightCm;
    }

    /**
     * 设置身高快照。
     */
    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    /**
     * 变更后体重千克。
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }

    /**
     * 设置体重快照。
     */
    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }
}
