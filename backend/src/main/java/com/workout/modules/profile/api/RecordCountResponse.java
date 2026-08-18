package com.workout.modules.profile.api;

/**
 * 某上海自然日的记录条数（API 边界 DTO）。
 */
public class RecordCountResponse {

    private final String date;
    private final long count;

    /**
     * 构造日条数。
     *
     * @param date  YYYY-MM-DD（Asia/Shanghai）
     * @param count 当日未删除记录数
     */
    public RecordCountResponse(String date, long count) {
        this.date = date;
        this.count = count;
    }

    /**
     * 自然日。
     */
    public String getDate() {
        return date;
    }

    /**
     * 条数。
     */
    public long getCount() {
        return count;
    }
}
