package com.workout.modules.share.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 分享报告快照持久化实体（持久化层）。
 * 对应表 work_out_share_report；公开 id 使用 token，不得把自增主键当分享链接。
 */
@Entity
@Table(name = "work_out_share_report")
public class ShareReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "range_from", nullable = false)
    private LocalDate rangeFrom;

    @Column(name = "range_to", nullable = false)
    private LocalDate rangeTo;

    @Column(name = "snapshot_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String snapshotJson;

    @Column(name = "advice_status", nullable = false, length = 16)
    private String adviceStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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
     * 公开 token。
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置公开 token。
     */
    public void setToken(String token) {
        this.token = token;
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
     * 筛选起日。
     */
    public LocalDate getRangeFrom() {
        return rangeFrom;
    }

    /**
     * 设置筛选起日。
     */
    public void setRangeFrom(LocalDate rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    /**
     * 筛选止日。
     */
    public LocalDate getRangeTo() {
        return rangeTo;
    }

    /**
     * 设置筛选止日。
     */
    public void setRangeTo(LocalDate rangeTo) {
        this.rangeTo = rangeTo;
    }

    /**
     * 冻结快照 JSON。
     */
    public String getSnapshotJson() {
        return snapshotJson;
    }

    /**
     * 设置快照 JSON。
     */
    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }

    /**
     * 建议分析状态（NONE_KEY/PENDING/READY/FAILED）。
     */
    public String getAdviceStatus() {
        return adviceStatus;
    }

    /**
     * 设置建议状态。
     */
    public void setAdviceStatus(String adviceStatus) {
        this.adviceStatus = adviceStatus;
    }

    /**
     * 创建时间。
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
}
