package com.workout.modules.share.api;

import java.time.Instant;
import java.time.LocalDate;

/**
 * C 端本人分享报告列表项（API 边界）。
 * id 为公开 token；禁止携带快照正文。
 */
public class MyShareListItemResponse {

    private final String id;
    private final LocalDate from;
    private final LocalDate to;
    private final Instant createdAt;

    /**
     * 构造本人分享摘要。
     */
    public MyShareListItemResponse(String id, LocalDate from, LocalDate to, Instant createdAt) {
        this.id = id;
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
