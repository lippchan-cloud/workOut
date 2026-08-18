package com.workout.modules.record.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * 已解析的日记录查询区间（领域值对象）。
 * 用上海时区闭开 Instant 区间表达单日 / 整月 / 自定义日期范围；不承担持久化，也不接受客户端 userId。
 */
public final class RecordQueryPeriod {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final LocalDate date;
    private final YearMonth yearMonth;
    private final LocalDate from;
    private final LocalDate to;

    /**
     * 内部构造：from/to 为上海时区含首含尾的自然日。
     */
    private RecordQueryPeriod(LocalDate date, YearMonth yearMonth, LocalDate from, LocalDate to) {
        this.date = date;
        this.yearMonth = yearMonth;
        this.from = from;
        this.to = to;
    }

    /**
     * 单日模式：区间为该日 00:00 至次日 00:00。
     */
    public static RecordQueryPeriod day(LocalDate date) {
        return new RecordQueryPeriod(date, null, date, date);
    }

    /**
     * 整月模式：from 为月初，to 为月末。
     */
    public static RecordQueryPeriod month(YearMonth yearMonth) {
        return new RecordQueryPeriod(null, yearMonth, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    /**
     * 自定义闭区间：from/to 均含当天。
     */
    public static RecordQueryPeriod range(LocalDate from, LocalDate to) {
        return new RecordQueryPeriod(null, null, from, to);
    }

    /**
     * 上海时区区间左闭端点。
     */
    public Instant startInclusive() {
        return from.atStartOfDay(SHANGHAI).toInstant();
    }

    /**
     * 上海时区区间右开端点（to 次日 00:00）。
     */
    public Instant endExclusive() {
        return to.plusDays(1).atStartOfDay(SHANGHAI).toInstant();
    }

    /**
     * xlsx 下载文件名：整月 `workout-YYYY-MM.xlsx`；同日 `workout-YYYY-MM-DD.xlsx`；跨日用下划线连接。
     */
    public String xlsxFilename() {
        if (yearMonth != null) {
            return "workout-" + yearMonth + ".xlsx";
        }
        if (from.equals(to)) {
            return "workout-" + from + ".xlsx";
        }
        return "workout-" + from + "_" + to + ".xlsx";
    }

    /**
     * 单日模式下的选中日；非单日为 null。
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 整月模式下的年月；非整月为 null。
     */
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    /**
     * 含首自然日。
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * 含尾自然日。
     */
    public LocalDate getTo() {
        return to;
    }
}
