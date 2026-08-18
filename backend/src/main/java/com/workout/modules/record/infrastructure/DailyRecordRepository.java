package com.workout.modules.record.infrastructure;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 按主键与用户加载未删除记录，一次查询完成隔离。
     */
    java.util.Optional<DailyRecordEntity> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    /**
     * 按用户批量物理删除全部日记录（注销账号），禁止循环 deleteById。
     */
    void deleteByUserId(Long userId);

    /**
     * 一次查出当前用户未删除记录的 recordedAt，供曲线按日聚合，禁止按日循环。
     */
    @Query("select r.recordedAt from DailyRecordEntity r where r.userId = :userId and r.deleted = false")
    List<Instant> findRecordedAtByUserIdAndDeletedFalse(@Param("userId") Long userId);
}
