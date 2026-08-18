package com.workout.bootstrap;

import com.workout.config.WorkoutAiProperties;
import com.workout.modules.admin.application.AdminApiKeyService;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时把配置中的 DeepSeek API Key 赋给 demo / lipp 等种子用户（启动层）。
 * 日志只打掩码；test profile 不跑。权威数据在 MySQL work_out_user_api_key。
 */
@Component
@Profile("!test")
@Order(100)
public class DeepSeekApiKeySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekApiKeySeeder.class);

    private final WorkoutAiProperties aiProperties;
    private final UserRepository userRepository;
    private final UserApiKeyRepository userApiKeyRepository;

    /**
     * 注入配置与仓储。
     */
    public DeepSeekApiKeySeeder(
            WorkoutAiProperties aiProperties,
            UserRepository userRepository,
            UserApiKeyRepository userApiKeyRepository) {
        this.aiProperties = aiProperties;
        this.userRepository = userRepository;
        this.userApiKeyRepository = userApiKeyRepository;
    }

    /**
     * 为配置名单中的已存在用户写入 / 覆盖 API Key。
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
        if (names == null || names.isBlank()) {
            log.info("[AI种子] run skip empty seedUsernames");
            return;
        }
        List<String> usernames = Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        // 批量按用户名查用户，禁止循环远程
        List<UserEntity> users = userRepository.findByUsernameIn(usernames);
        if (users.isEmpty()) {
            log.info("[AI种子] run skip no matching users");
            return;
        }
        String mask = AdminApiKeyService.mask(apiKey.trim());
        Instant now = Instant.now();
        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        var existing = userApiKeyRepository.findByUserIdIn(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(UserApiKeyEntity::getUserId, e -> e, (a, b) -> a));
        List<UserApiKeyEntity> toSave = users.stream()
                .map(u -> {
                    UserApiKeyEntity row = existing.getOrDefault(u.getId(), new UserApiKeyEntity());
                    row.setUserId(u.getId());
                    row.setApiKey(apiKey.trim());
                    row.setKeyMask(mask);
                    row.setUpdatedAt(now);
                    row.setUpdatedBy(null);
                    return row;
                })
                .toList();
        userApiKeyRepository.saveAll(toSave);
        log.info(
                "[AI种子] run done keyMask={}, assignedUserIds={}, size={}",
                mask,
                userIds,
                toSave.size());
    }
}
