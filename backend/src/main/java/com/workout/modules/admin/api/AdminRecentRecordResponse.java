package com.workout.modules.admin.api;

import com.workout.modules.record.domain.RecordType;
import java.time.Instant;

/**
 * CMS 用户详情中的最近日记录摘要（API 边界）。
 * 不含跨用户数据，仅只读展示。
 */
public class AdminRecentRecordResponse {

    private final Long id;
    private final RecordType type;
    private final String content;
    private final Instant recordedAt;

    /**
     * 构造最近记录摘要。
     */
    public AdminRecentRecordResponse(Long id, RecordType type, String content, Instant recordedAt) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.recordedAt = recordedAt;
    }

    /**
     * 记录主键。
     */
    public Long getId() {
        return id;
    }

    /**
     * 消耗或摄入。
     */
    public RecordType getType() {
        return type;
    }

    /**
     * 记录内容。
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
