package com.workout.modules.ai.infrastructure;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * AI 调用日志仓储（数据访问层）。
 * 限流计数必须走聚合查询。
 */
public interface AiCallLogRepository extends JpaRepository<AiCallLogEntity, Long> {

    /**
     * 按 apiKey 与时间窗口计数（限流）。
     */
    @Query(
            "select count(l) from AiCallLogEntity l where l.apiKeyId = :apiKeyId and l.createdAt >= :since")
    long countByApiKeyIdSince(@Param("apiKeyId") Long apiKeyId, @Param("since") Instant since);

    /**
     * CMS：按用户筛选，新在前。
     */
    List<AiCallLogEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * CMS：按 apiKey 筛选。
     */
    List<AiCallLogEntity> findByApiKeyIdOrderByCreatedAtDesc(Long apiKeyId);

    /**
     * CMS：用户 + apiKey 双条件。
     */
    List<AiCallLogEntity> findByUserIdAndApiKeyIdOrderByCreatedAtDesc(Long userId, Long apiKeyId);

    /**
     * CMS：无筛选时列出最近调用（全表按时间倒序，体量小可接受）。
     */
    List<AiCallLogEntity> findAllByOrderByCreatedAtDesc();
}
