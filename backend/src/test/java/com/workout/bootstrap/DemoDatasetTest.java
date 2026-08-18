package com.workout.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.modules.record.domain.RecordType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class DemoDatasetTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Test
    void buildShouldCoverAboutThreeMonthsPastAndFutureWithMixedConsumeIntake() {
        Instant now = Instant.parse("2026-08-18T09:00:00+08:00");
        Clock clock = Clock.fixed(now, SHANGHAI);
        DemoDataset dataset = DemoDataset.build(clock);
        LocalDate today = LocalDate.of(2026, 8, 18);
        assertThat(dataset.records()).isNotEmpty();
        assertThat(dataset.history()).isNotEmpty();
        assertThat(dataset.records().stream().map(DemoDataset.PlannedRecord::type))
                .contains(RecordType.CONSUME, RecordType.INTAKE);
        assertThat(dataset.records().stream().map(DemoDataset.PlannedRecord::content))
                .anyMatch(content -> content.matches(".*[\\u4e00-\\u9fff].*"))
                .anyMatch(content -> content.matches(".*[A-Za-z].*"));
        LocalDate min = dataset.records().stream()
                .map(row -> LocalDate.ofInstant(row.recordedAt(), SHANGHAI))
                .min(LocalDate::compareTo)
                .orElseThrow();
        LocalDate max = dataset.records().stream()
                .map(row -> LocalDate.ofInstant(row.recordedAt(), SHANGHAI))
                .max(LocalDate::compareTo)
                .orElseThrow();
        assertThat(ChronoUnit.DAYS.between(min, today)).isGreaterThanOrEqualTo(80);
        assertThat(ChronoUnit.DAYS.between(today, max)).isGreaterThanOrEqualTo(80);
        assertThat(dataset.history().size()).isGreaterThanOrEqualTo(6);
        assertThat(dataset.currentHeightCm()).isNotNull();
        assertThat(dataset.currentWeightKg()).isNotNull();
    }
}
