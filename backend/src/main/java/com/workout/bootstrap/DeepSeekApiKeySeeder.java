package com.workout.bootstrap;

import com.workout.config.WorkoutAiProperties;
import com.workout.modules.admin.application.AdminApiKeyService;
import com.workout.modules.ai.application.ApiKeyAssignmentService;
import com.workout.modules.ai.infrastructure.ApiKeyPoolEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时把配置中的 DeepSeek Key 写入密钥库，并赋给 demo / lipp（启动层）。
 * 日志只打掩码；test profile 不跑。权威在 MySQL work_out_api_key。
 */
@Component
@Profile("!test")
@Order(100)
public class DeepSeekApiKeySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekApiKeySeeder.class);

    private final WorkoutAiProperties aiProperties;
    private final UserRepository userRepository;
    private final UserApiKeyRepository userApiKeyRepository;
    private final ApiKeyAssignmentService apiKeyAssignmentService;

    /**
     * 注入配置、用户仓储与分配服务。
     */
    public DeepSeekApiKeySeeder(
            WorkoutAiProperties aiProperties,
            UserRepository userRepository,
            UserApiKeyRepository userApiKeyRepository,
            ApiKeyAssignmentService apiKeyAssignmentService) {
        this.aiProperties = aiProperties;
        this.userRepository = userRepository;
        this.userApiKeyRepository = userApiKeyRepository;
        this.apiKeyAssignmentService = apiKeyAssignmentService;
    }

    /**
     * 密钥入库后，给配置名单中尚未绑定的用户批量绑定。
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String apiKey = aiProperties.getDeepseek().getApiKey();
        String names = aiProperties.getDeepseek().getSeedUsernames();
        log.info(
                "[AI种子] run start keyMask={}, seedUsernames={}",
                AdminApiKeyService.mask(apiKey),
                names);
        if (apiKey == null || apiKey.isBlank()) {
            log.info("[AI种子] run skip empty apiKey");
            return;
        }
        ApiKeyPoolEntity pool = apiKeyAssignmentService.upsertPool(apiKey.trim(), null);
        log.info("[AI种子] pool entityType=ApiKeyPoolEntity id={}, keyMask={}", pool.getId(), pool.getKeyMask());
        if (names == null || names.isBlank()) {
            log.info("[AI种子] run skip empty seedUsernames");
            return;
        }
        List<String> usernames = Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<UserEntity> users = userRepository.findByUsernameIn(usernames);
        if (users.isEmpty()) {
            log.info("[AI种子] run skip no matching users");
            return;
        }
        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        Set<Long> alreadyBound = userApiKeyRepository.findByUserIdIn(userIds).stream()
                .map(UserApiKeyEntity::getUserId)
                .collect(Collectors.toSet());
        Instant now = Instant.now();
        List<UserApiKeyEntity> toSave = users.stream()
                .filter(u -> !alreadyBound.contains(u.getId()))
                .map(u -> {
                    UserApiKeyEntity row = new UserApiKeyEntity();
                    row.setUserId(u.getId());
                    row.setPoolId(pool.getId());
                    row.setApiKey(pool.getApiKey());
                    row.setKeyMask(pool.getKeyMask());
                    row.setUpdatedAt(now);
                    return row;
                })
                .toList();
        if (!toSave.isEmpty()) {
            userApiKeyRepository.saveAll(toSave);
        }
        log.info("[AI种子] run done poolId={}, newlyBound={}", pool.getId(), toSave.size());
    }
}
