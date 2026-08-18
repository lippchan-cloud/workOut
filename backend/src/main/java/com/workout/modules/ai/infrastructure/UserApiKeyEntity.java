package com.workout.modules.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 用户–DeepSeek API Key 关联（持久化层）。
 * 对应 work_out_user_api_key；一用户一行；日志禁止打印 apiKey 全文。
 */
@Entity
@Table(name = "work_out_user_api_key")
public class UserApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "api_key", nullable = false, length = 256)
    private String apiKey;

    @Column(name = "key_mask", nullable = false, length = 32)
    private String keyMask;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

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
     * 绑定用户。
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
     * DeepSeek API Key（库内明文，私有仓约定）。
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 设置 API Key。
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * CMS 展示用掩码。
     */
    public String getKeyMask() {
        return keyMask;
    }

    /**
     * 设置掩码。
     */
    public void setKeyMask(String keyMask) {
        this.keyMask = keyMask;
    }

    /**
     * 最后更新时间。
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

    /**
     * 操作管理员 userId。
     */
    public Long getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 设置操作者。
     */
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
