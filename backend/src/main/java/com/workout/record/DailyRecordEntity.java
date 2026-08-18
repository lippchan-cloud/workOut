package com.workout.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 日记录持久化实体（持久化层）。
 * 对应表 daily_record；userId 必须来自服务端鉴权，不得由客户端覆盖。
 */
@Entity
@Table(name = "daily_record")
public class DailyRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecordType type;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, columnDefinition = "tinyint")
    private boolean deleted = false;

    /**
     * 主键。
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键。
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 所属用户。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置所属用户。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 记录类型。
     */
    public RecordType getType() {
        return type;
    }

    /**
     * 设置记录类型。
     */
    public void setType(RecordType type) {
        this.type = type;
    }

    /**
     * 文本内容。
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置文本内容。
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 用户确认的记录时间。
     */
    public Instant getRecordedAt() {
        return recordedAt;
    }

    /**
     * 设置记录时间。
     */
    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * 系统创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 是否逻辑删除。
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 设置逻辑删除标记。
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
