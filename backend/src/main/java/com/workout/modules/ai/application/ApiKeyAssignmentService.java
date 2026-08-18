package com.workout.modules.ai.application;

import com.workout.modules.admin.application.AdminApiKeyService;
import com.workout.modules.ai.infrastructure.ApiKeyPoolEntity;
import com.workout.modules.ai.infrastructure.ApiKeyPoolRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 把密钥库中的 Key 绑定到用户（应用层）。
 * 新注册默认取启用且绑定最少的一把；已绑定则跳过。
 */
@Service
public class ApiKeyAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAssignmentService.class);

    private final ApiKeyPoolRepository apiKeyPoolRepository;
    private final UserApiKeyRepository userApiKeyRepository;

    /**
     * 注入仓储。
     */
    public ApiKeyAssignmentService(
            ApiKeyPoolRepository apiKeyPoolRepository, UserApiKeyRepository userApiKeyRepository) {
        this.apiKeyPoolRepository = apiKeyPoolRepository;
        this.userApiKeyRepository = userApiKeyRepository;
    }

    /**
     * 若用户尚无绑定，从密钥库分配一把。库空则跳过。
     */
    @Transactional
    public void assignDefaultIfAbsent(Long userId) {
        log.info("[API密钥] assignDefaultIfAbsent start userId={}", userId);
        if (userApiKeyRepository.findByUserId(userId).isPresent()) {
            log.info("[API密钥] assignDefaultIfAbsent skip already bound userId={}", userId);
            return;
        }
        // 一次 SQL 取绑定最少的启用 key id，再按 id 加载实体（避免 ONLY_FULL_GROUP_BY）
        Long poolId = apiKeyPoolRepository.findLeastUsedEnabledId().orElse(null);
        if (poolId == null) {
            log.info("[API密钥] assignDefaultIfAbsent skip empty pool userId={}", userId);
            return;
        }
        ApiKeyPoolEntity pool = apiKeyPoolRepository.findById(poolId).orElse(null);
        if (pool == null) {
            log.info("[API密钥] assignDefaultIfAbsent skip missing poolId={} userId={}", poolId, userId);
            return;
        }
        bind(userId, pool, null);
        log.info(
                "[API密钥] assignDefaultIfAbsent done userId={}, poolId={}, keyMask={}",
                userId,
                pool.getId(),
                pool.getKeyMask());
    }

    /**
     * 将指定密钥库条目绑定到用户（覆盖原绑定）。
     */
    @Transactional
    public UserApiKeyEntity bind(Long userId, ApiKeyPoolEntity pool, Long operatorUserId) {
        log.info(
                "[API密钥] bind start userId={}, poolId={}, keyMask={}",
                userId,
                pool.getId(),
                pool.getKeyMask());
        UserApiKeyEntity row = userApiKeyRepository.findByUserId(userId).orElseGet(UserApiKeyEntity::new);
        row.setUserId(userId);
        row.setPoolId(pool.getId());
        row.setApiKey(pool.getApiKey());
        row.setKeyMask(pool.getKeyMask());
        row.setUpdatedAt(Instant.now());
        row.setUpdatedBy(operatorUserId);
        UserApiKeyEntity saved = userApiKeyRepository.save(row);
        log.info(
                "[API密钥] bind done entityType=UserApiKeyEntity id={}, userId={}, poolId={}",
                saved.getId(),
                userId,
                pool.getId());
        return saved;
    }

    /**
     * 按明文 key 找到或写入密钥库（CMS / 种子共用）。
     */
    @Transactional
    public ApiKeyPoolEntity upsertPool(String apiKey, Long createdBy) {
        String trimmed = apiKey.trim();
        String mask = AdminApiKeyService.mask(trimmed);
        log.info("[API密钥] upsertPool start keyMask={}, createdBy={}", mask, createdBy);
        ApiKeyPoolEntity pool = apiKeyPoolRepository.findByApiKey(trimmed).orElseGet(ApiKeyPoolEntity::new);
        boolean creating = pool.getId() == null;
        pool.setApiKey(trimmed);
        pool.setKeyMask(mask);
        pool.setEnabled(true);
        if (creating) {
            pool.setCreatedAt(Instant.now());
            pool.setCreatedBy(createdBy);
        }
        ApiKeyPoolEntity saved = apiKeyPoolRepository.save(pool);
        log.info(
                "[API密钥] upsertPool done entityType=ApiKeyPoolEntity id={}, keyMask={}, creating={}",
                saved.getId(),
                saved.getKeyMask(),
                creating);
        return saved;
    }
}
