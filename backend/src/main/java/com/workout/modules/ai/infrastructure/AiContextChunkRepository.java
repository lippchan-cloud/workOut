package com.workout.modules.ai.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 上下文摘要仓储（数据访问层）。
 * 查询必须带 userId，禁止跨用户复用。
 */
public interface AiContextChunkRepository extends JpaRepository<AiContextChunkEntity, Long> {

    /**
     * 同用户同 hash 命中则复用摘要。
     */
    Optional<AiContextChunkEntity> findFirstByUserIdAndEmbedHashOrderByCreatedAtDesc(
            Long userId, String embedHash);

    /**
     * 按用户列出（调试/审计，批量）。
     */
    List<AiContextChunkEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 取该用户最近若干条压缩询问，供拼 prompt（一次查出，禁止循环）。
     */
    List<AiContextChunkEntity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
