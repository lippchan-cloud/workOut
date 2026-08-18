package com.workout.modules.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * AI 调用日志（持久化层）。
 * 对应 work_out_ai_call_log；限流与 CMS 筛选依赖 SQL 聚合，禁止拉全表内存计数。
 */
@Entity
@Table(name = "work_out_ai_call_log")
public class AiCallLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @Column(nullable = false, length = 32)
    private String purpose;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "share_token", length = 64)
    private String shareToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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
     * 发起用户。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 使用的 apiKey 行 id。
     */
    public Long getApiKeyId() {
        return apiKeyId;
    }

    /**
     * 设置 apiKeyId。
     */
    public void setApiKeyId(Long apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    /**
     * 用途常量名。
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * 设置用途。
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * 调用结果状态。
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态。
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 关联分享 token。
     */
    public String getShareToken() {
        return shareToken;
    }

    /**
     * 设置分享 token。
     */
    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    /**
     * 创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
