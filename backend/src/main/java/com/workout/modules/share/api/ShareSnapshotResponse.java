package com.workout.modules.share.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 公开报告快照（API 边界 DTO）。
 * advice 预留建议分析，现阶段为空。
 */
public class ShareSnapshotResponse {

    private String from;
    private String to;
    private String displayName;
    private List<ShareRecordItem> records;
    private List<ShareBodyPoint> bodyHistory;
    private String advice;

    /**
     * 区间起日。
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置区间起日。
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * 区间止日。
     */
    public String getTo() {
        return to;
    }

    /**
     * 设置区间止日。
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * 用户显示名。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名。
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 事项列表。
     */
    public List<ShareRecordItem> getRecords() {
        return records;
    }

    /**
     * 设置事项列表。
     */
    public void setRecords(List<ShareRecordItem> records) {
        this.records = records;
    }

    /**
     * 成长曲线点。
     */
    public List<ShareBodyPoint> getBodyHistory() {
        return bodyHistory;
    }

    /**
     * 设置成长曲线点。
     */
    public void setBodyHistory(List<ShareBodyPoint> bodyHistory) {
        this.bodyHistory = bodyHistory;
    }

    /**
     * 建议分析（空占位）。
     */
    public String getAdvice() {
        return advice;
    }

    /**
     * 设置建议分析。
     */
    public void setAdvice(String advice) {
        this.advice = advice;
    }

    /**
     * 快照中的一条事项。
     */
    public static class ShareRecordItem {
        private Instant recordedAt;
        private String type;
        private String content;

        /**
         * 记录时间。
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
         * 类型。
         */
        public String getType() {
            return type;
        }

        /**
         * 设置类型。
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * 内容。
         */
        public String getContent() {
            return content;
        }

        /**
         * 设置内容。
         */
        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 快照中的身体历史点。
     */
    public static class ShareBodyPoint {
        private Instant changedAt;
        private BigDecimal heightCm;
        private BigDecimal weightKg;

        /**
         * 变更时间。
         */
        public Instant getChangedAt() {
            return changedAt;
        }

        /**
         * 设置变更时间。
         */
        public void setChangedAt(Instant changedAt) {
            this.changedAt = changedAt;
        }

        /**
         * 身高厘米。
         */
        public BigDecimal getHeightCm() {
            return heightCm;
        }

        /**
         * 设置身高。
         */
        public void setHeightCm(BigDecimal heightCm) {
            this.heightCm = heightCm;
        }

        /**
         * 体重千克。
         */
        public BigDecimal getWeightKg() {
            return weightKg;
        }

        /**
         * 设置体重。
         */
        public void setWeightKg(BigDecimal weightKg) {
            this.weightKg = weightKg;
        }
    }
}
