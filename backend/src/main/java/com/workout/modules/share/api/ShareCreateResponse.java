package com.workout.modules.share.api;

/**
 * 创建分享成功响应（API 边界 DTO）。
 */
public class ShareCreateResponse {

    private final String id;
    private final String url;

    /**
     * 构造响应：公开 id 即 token。
     */
    public ShareCreateResponse(String id, String url) {
        this.id = id;
        this.url = url;
    }

    /**
     * 公开 token。
     */
    public String getId() {
        return id;
    }

    /**
     * 可访问 H5 完整链接。
     */
    public String getUrl() {
        return url;
    }
}
