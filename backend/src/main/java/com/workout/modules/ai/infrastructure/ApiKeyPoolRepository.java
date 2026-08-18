package com.workout.modules.ai.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 密钥库仓储（数据访问层）。
 * 分配时一次 SQL 取绑定最少的启用 key，禁止循环计数。
 */
public interface ApiKeyPoolRepository extends JpaRepository<ApiKeyPoolEntity, Long> {

    /**
     * 按完整 key 查找，供去重入库。
     */
    Optional<ApiKeyPoolEntity> findByApiKey(String apiKey);

    /**
     * 取启用且当前绑定用户最少的一把的 id（仅 SELECT id，兼容 ONLY_FULL_GROUP_BY）。
     */
    @Query(
            value =
                    """
                    SELECT p.id FROM work_out_api_key p
                    LEFT JOIN work_out_user_api_key u ON u.pool_id = p.id
                    WHERE p.enabled = 1
                    GROUP BY p.id
                    ORDER BY COUNT(u.id) ASC, p.id ASC
                    LIMIT 1
                    """,
            nativeQuery = true)
    Optional<Long> findLeastUsedEnabledId();
}
