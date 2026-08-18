package com.workout.modules.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * DeepSeek API Key 密钥库（持久化层）。
 * 对应 work_out_api_key；CMS 增删改；新用户从本表取一把绑定。日志禁止打印 api_key 全文。
 */
@Entity
@Table(name = "work_out_api_key")
public class ApiKeyPoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", nullable = false, unique = true, length = 256)
    private String apiKey;

    @Column(name = "key_mask", nullable = false, length = 32)
    private String keyMask;

    @Column(nullable = false, columnDefinition = "tinyint")
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

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
     * 完整 API Key（库内明文）。
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
     * CMS 掩码。
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
     * 是否可用于新用户分配。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    /**
     * 创建人管理员 id。
     */
    public Long getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人。
     */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
