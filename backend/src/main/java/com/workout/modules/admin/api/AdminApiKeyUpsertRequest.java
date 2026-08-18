package com.workout.modules.admin.api;

import java.util.List;

/**
 * 单用户改 API Key 请求体（内层 request）。
 */
public class AdminApiKeyUpsertRequest {

    private String apiKey;

    /**
     * API Key 明文（仅写入，响应不回显全文）。
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
