package com.workout.bootstrap;

import com.workout.modules.record.domain.RecordType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 演示种子数据集（启动辅助，非业务编排）。
 * 只在内存中按 Clock 生成计划，不访问数据库。
 */
public final class DemoDataset {

    public static final String USERNAME = "demo";
    public static final String PASSWORD = "demo1234";
    public static final String NICKNAME = "Demo";

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Logger log = LoggerFactory.getLogger(DemoDataset.class);
    private static final String[] CONSUME = {
        "跑步", "easy run", "力量训练", "strength session", "骑行", "cycling laps", "游泳", "swim drills"
    };
    private static final String[] INTAKE = {
        "燕麦", "oats bowl", "鸡胸沙拉", "chicken salad", "蛋白奶昔", "protein shake", "牛肉面", "beef noodles"
    };

    private final List<PlannedRecord> records;
    private final List<PlannedHistory> history;
    private final BigDecimal currentHeightCm;
    private final BigDecimal currentWeightKg;

    /**
     * @param records 计划中的日记录
     * @param history 计划中的身体历史
     * @param currentHeightCm 当前身高
     * @param currentWeightKg 当前体重
     */
    private DemoDataset(
            List<PlannedRecord> records,
            List<PlannedHistory> history,
            BigDecimal currentHeightCm,
            BigDecimal currentWeightKg) {
        this.records = List.copyOf(records);
        this.history = List.copyOf(history);
        this.currentHeightCm = currentHeightCm;
        this.currentWeightKg = currentWeightKg;
    }

    /**
     * 以给定时钟的「今天」为中心，生成约过去 90 天与未来 90 天的演示数据。
     *
     * @param clock 用于确定「今天」的时钟
     * @return 不可变计划
     */
    public static DemoDataset build(Clock clock) {
        // 关键入口：只记录时钟时区与瞬时，禁止打印密码
        log.info("[演示种子] DemoDataset.build start zone={}, instant={}", clock.getZone(), clock.instant());
        LocalDate today = LocalDate.ofInstant(clock.instant(), SHANGHAI);
        LocalDate start = today.minusDays(90);
        LocalDate end = today.plusDays(90);
        List<PlannedRecord> records = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            int dow = day.getDayOfWeek().getValue();
            int seed = day.getDayOfYear();
            if (dow == 1) {
                records.add(new PlannedRecord(RecordType.CONSUME, CONSUME[seed % CONSUME.length], at(day, 7, 30)));
            }
            if (dow == 3) {
                records.add(new PlannedRecord(RecordType.CONSUME, CONSUME[(seed + 3) % CONSUME.length], at(day, 18, 0)));
            }
            if (dow == 5) {
                records.add(new PlannedRecord(RecordType.INTAKE, INTAKE[seed % INTAKE.length], at(day, 12, 15)));
            }
            if (dow == 6) {
                records.add(new PlannedRecord(RecordType.INTAKE, INTAKE[(seed + 2) % INTAKE.length], at(day, 8, 0)));
            }
        }
        List<PlannedHistory> history = new ArrayList<>();
        YearMonth cursor = YearMonth.from(start);
        YearMonth lastMonth = YearMonth.from(end);
        int monthIndex = 0;
        while (!cursor.isAfter(lastMonth)) {
            BigDecimal height = new BigDecimal("172.0").add(new BigDecimal("0.5").multiply(BigDecimal.valueOf(monthIndex)));
            BigDecimal weight = new BigDecimal("74.0").subtract(new BigDecimal("0.3").multiply(BigDecimal.valueOf(monthIndex)));
            Instant changedAt = cursor.atDay(1).atTime(8, 0).atZone(SHANGHAI).toInstant();
            history.add(new PlannedHistory(changedAt, NICKNAME, height, weight));
            cursor = cursor.plusMonths(1);
            monthIndex++;
        }
        PlannedHistory latest = history.get(history.size() - 1);
        DemoDataset dataset = new DemoDataset(records, history, latest.heightCm(), latest.weightKg());
        log.info(
                "[演示种子] DemoDataset.build done records={}, history={}, from={}, to={}",
                records.size(),
                history.size(),
                start,
                end);
        return dataset;
    }

    /**
     * 把上海本地时分编成瞬时。
     */
    private static Instant at(LocalDate day, int hour, int minute) {
        return day.atTime(hour, minute).atZone(SHANGHAI).toInstant();
    }

    /**
     * @return 日记录计划
     */
    public List<PlannedRecord> records() {
        return records;
    }

    /**
     * @return 身体历史计划
     */
    public List<PlannedHistory> history() {
        return history;
    }

    /**
     * @return 当前身高厘米
     */
    public BigDecimal currentHeightCm() {
        return currentHeightCm;
    }

    /**
     * @return 当前体重千克
     */
    public BigDecimal currentWeightKg() {
        return currentWeightKg;
    }

    /**
     * 一条计划中的日记录。
     */
    public record PlannedRecord(RecordType type, String content, Instant recordedAt) {}

    /**
     * 一条计划中的身体历史快照。
     */
    public record PlannedHistory(Instant changedAt, String nickname, BigDecimal heightCm, BigDecimal weightKg) {}
}
