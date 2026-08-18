package com.workout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项（配置层）。
 * 从 application.yml 的 workout.jwt 读取密钥与过期天数，不参与业务编排。
 */
@ConfigurationProperties(prefix = "workout.jwt")
public class JwtProperties {

    private String secret;
    private long expireDays = 7;

    /**
     * 读取 HMAC 签名密钥。
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 设置 HMAC 签名密钥。
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 读取 Token 有效天数。
     */
    public long getExpireDays() {
        return expireDays;
    }

    /**
     * 设置 Token 有效天数。
     */
    public void setExpireDays(long expireDays) {
        this.expireDays = expireDays;
    }
}
