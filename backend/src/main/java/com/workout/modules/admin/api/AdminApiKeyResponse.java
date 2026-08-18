package com.workout.modules.admin.api;

/**
 * CMS 用户 API Key 视图（API 边界 DTO）。
 * 仅含掩码，不得带完整 key。
 */
public class AdminApiKeyResponse {

    private final Long userId;
    private final String username;
    private final Long apiKeyId;
    private final String keyMask;
    private final boolean hasKey;

    /**
     * @param userId   用户
     * @param username 用户名
     * @param apiKeyId key 行 id，可空
     * @param keyMask  掩码，可空
     * @param hasKey   是否已绑定
     */
    public AdminApiKeyResponse(Long userId, String username, Long apiKeyId, String keyMask, boolean hasKey) {
        this.userId = userId;
        this.username = username;
        this.apiKeyId = apiKeyId;
        this.keyMask = keyMask;
        this.hasKey = hasKey;
    }

    /**
     * 用户 id。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 用户名。
     */
    public String getUsername() {
        return username;
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
     * 是否已绑定。
     */
    public boolean isHasKey() {
        return hasKey;
    }
}
