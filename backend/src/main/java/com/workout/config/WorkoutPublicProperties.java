package com.workout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 公开访问基址配置（配置层）。
 * 用于拼接分享 H5 链接，禁止写死局域网 IP。
 */
@ConfigurationProperties(prefix = "workout")
public class WorkoutPublicProperties {

    private String publicBaseUrl = "http://localhost:8080";

    /**
     * 读取公开基址（无尾斜杠）。
     */
    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    /**
     * 设置公开基址。
     */
    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? "http://localhost:8080"
                : publicBaseUrl.replaceAll("/+$", "");
    }
}
