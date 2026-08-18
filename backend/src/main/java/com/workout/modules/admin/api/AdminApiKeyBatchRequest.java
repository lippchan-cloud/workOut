package com.workout.modules.admin.api;

import java.util.List;

/**
 * 批量改 API Key 请求体（内层 request）。
 */
public class AdminApiKeyBatchRequest {

    private List<Long> userIds;
    private String apiKey;

    /**
     * 目标用户 id 列表。
     */
    public List<Long> getUserIds() {
        return userIds;
    }

    /**
     * 设置用户列表。
     */
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    /**
     * 共用 API Key。
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 设置 apiKey。
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
