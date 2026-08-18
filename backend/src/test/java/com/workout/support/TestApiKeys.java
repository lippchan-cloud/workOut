package com.workout.support;

import com.workout.modules.admin.application.AdminApiKeyService;
import com.workout.modules.ai.infrastructure.ApiKeyPoolEntity;
import com.workout.modules.ai.infrastructure.ApiKeyPoolRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import java.time.Instant;

/**
 * 测试里绑定假 DeepSeek Key：先写入密钥库再绑用户。
 */
public final class TestApiKeys {

    private TestApiKeys() {}

    /**
     * 将 fakeKey 放入密钥库并绑定 userId。
     */
    public static UserApiKeyEntity bind(
            ApiKeyPoolRepository poolRepository, UserApiKeyRepository userApiKeyRepository, Long userId, String fakeKey) {
        ApiKeyPoolEntity pool = poolRepository.findByApiKey(fakeKey).orElseGet(() -> {
            ApiKeyPoolEntity created = new ApiKeyPoolEntity();
            created.setApiKey(fakeKey);
            created.setKeyMask(AdminApiKeyService.mask(fakeKey));
            created.setEnabled(true);
            created.setCreatedAt(Instant.now());
            return poolRepository.save(created);
        });
        UserApiKeyEntity row = userApiKeyRepository.findByUserId(userId).orElseGet(UserApiKeyEntity::new);
        row.setUserId(userId);
        row.setPoolId(pool.getId());
        row.setApiKey(pool.getApiKey());
        row.setKeyMask(pool.getKeyMask());
        row.setUpdatedAt(Instant.now());
        return userApiKeyRepository.save(row);
    }
}
