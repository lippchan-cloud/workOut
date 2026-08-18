package com.workout.modules.admin.api;

import java.time.Instant;

/**
 * CMS AI 调用日志行（API 边界 DTO）。
 * 仅掩码，禁止完整 API Key。
 */
public class AdminAiCallResponse {

    private final Long id;
    private final Long userId;
    private final Long apiKeyId;
    private final String keyMask;
    private final String purpose;
    private final String status;
    private final String shareToken;
    private final Instant createdAt;

    /**
     * 组装一行调用记录。
     */
    public AdminAiCallResponse(
            Long id,
            Long userId,
            Long apiKeyId,
            String keyMask,
            String purpose,
            String status,
            String shareToken,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        this.keyMask = keyMask;
        this.purpose = purpose;
        this.status = status;
        this.shareToken = shareToken;
        this.createdAt = createdAt;
    }

    /**
     * 日志 id。
     */
    public Long getId() {
        return id;
    }

    /**
     * 用户 id。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * apiKey 行 id。
     */
    public Long getApiKeyId() {
        return apiKeyId;
    }

    /**
     * 掩码。
     */
    public String getKeyMask() {
        return keyMask;
    }

    /**
     * 用途。
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * 状态。
     */
    public String getStatus() {
        return status;
    }

    /**
     * 分享 token。
     */
    public String getShareToken() {
        return shareToken;
    }

    /**
     * 创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
