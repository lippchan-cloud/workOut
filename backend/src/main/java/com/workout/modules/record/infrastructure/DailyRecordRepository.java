package com.workout.modules.record.infrastructure;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 日记录仓储（数据访问层）。
 * 查询必须带 userId，禁止跨用户读取。
 */
public interface DailyRecordRepository extends JpaRepository<DailyRecordEntity, Long> {

    /**
     * 按用户与记录时间区间查询，按 recordedAt、id 升序。
     */
    List<DailyRecordEntity> findByUserIdAndDeletedFalseAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAscIdAsc(
            Long userId, Instant startInclusive, Instant endExclusive);
}
