package com.workout.modules.admin.api;

/**
 * CMS 密钥库条目（API 边界 DTO）。
 * 仅掩码，不得带完整 key。
 */
public class AdminApiKeyPoolResponse {

    private final Long id;
    private final String keyMask;
    private final boolean enabled;

    /**
     * @param id      密钥库 id
     * @param keyMask 掩码
     * @param enabled 是否启用
     */
    public AdminApiKeyPoolResponse(Long id, String keyMask, boolean enabled) {
        this.id = id;
        this.keyMask = keyMask;
        this.enabled = enabled;
    }

    /**
     * 密钥库 id。
     */
    public Long getId() {
        return id;
    }

    /**
     * 掩码。
     */
    public String getKeyMask() {
        return keyMask;
    }

    /**
     * 是否启用。
     */
    public boolean isEnabled() {
        return enabled;
    }
}
