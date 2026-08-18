package com.workout.modules.admin.api;

import java.time.Instant;
import java.time.LocalDate;

/**
 * CMS 已有分享报告列表项（API 边界）。
 * id 为公开 token；禁止携带快照正文或密码。
 */
public class AdminShareListItemResponse {

    private final String id;
    private final Long userId;
    private final String username;
    private final LocalDate from;
    private final LocalDate to;
    private final Instant createdAt;

    /**
     * 构造只读分享摘要。
     */
    public AdminShareListItemResponse(
            String id, Long userId, String username, LocalDate from, LocalDate to, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.from = from;
        this.to = to;
        this.createdAt = createdAt;
    }

    /**
     * 公开 token，对应 /report/{id}。
     */
    public String getId() {
        return id;
    }

    /**
     * 分享所属用户。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 登录用户名；用户已删则为 null。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 快照起日。
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * 快照止日。
     */
    public LocalDate getTo() {
        return to;
    }

    /**
     * 分享创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
