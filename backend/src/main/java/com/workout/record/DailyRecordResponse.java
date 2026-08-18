package com.workout.record;

import java.time.Instant;

/**
 * 日记录响应 DTO（API 边界）。
 */
public class DailyRecordResponse {

    private final Long id;
    private final RecordType type;
    private final String content;
    private final Instant recordedAt;

    /**
     * 构造响应。
     */
    public DailyRecordResponse(Long id, RecordType type, String content, Instant recordedAt) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.recordedAt = recordedAt;
    }

    /**
     * 从实体转换。
     */
    public static DailyRecordResponse from(DailyRecordEntity entity) {
        return new DailyRecordResponse(
                entity.getId(), entity.getType(), entity.getContent(), entity.getRecordedAt());
    }

    /**
     * 主键。
     */
    public Long getId() {
        return id;
    }

    /**
     * 类型。
     */
    public RecordType getType() {
        return type;
    }

    /**
     * 内容。
     */
    public String getContent() {
        return content;
    }

    /**
     * 记录时间。
     */
    public Instant getRecordedAt() {
        return recordedAt;
    }
}
