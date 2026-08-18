package com.workout.modules.profile.api;

import java.util.List;

/**
 * 变化曲线页数据（API 边界 DTO）。
 * 含身体历史快照与按日记录条数，不含他人数据。
 */
public class ProfileTrendsResponse {

    private final List<ProfileHistoryItemResponse> bodyHistory;
    private final List<RecordCountResponse> recordCounts;

    /**
     * 构造曲线数据。
     */
    public ProfileTrendsResponse(List<ProfileHistoryItemResponse> bodyHistory, List<RecordCountResponse> recordCounts) {
        this.bodyHistory = bodyHistory;
        this.recordCounts = recordCounts;
    }

    /**
     * 身体快照，changedAt 升序。
     */
    public List<ProfileHistoryItemResponse> getBodyHistory() {
        return bodyHistory;
    }

    /**
     * 按日条数，日期升序。
     */
    public List<RecordCountResponse> getRecordCounts() {
        return recordCounts;
    }
}
