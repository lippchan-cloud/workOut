package com.workout.modules.admin.application;

import com.workout.common.ForbiddenException;
import com.workout.modules.admin.api.AdminAiCallResponse;
import com.workout.modules.ai.infrastructure.AiCallLogEntity;
import com.workout.modules.ai.infrastructure.AiCallLogRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CMS AI 调用情况（应用层）。
 * 筛选走 SQL；掩码批量拼装，禁止 N+1。
 */
@Service
public class AdminAiCallService {

    private static final Logger log = LoggerFactory.getLogger(AdminAiCallService.class);

    private final UserRepository userRepository;
    private final AiCallLogRepository aiCallLogRepository;
    private final UserApiKeyRepository userApiKeyRepository;

    /**
     * 注入仓储。
     */
    public AdminAiCallService(
            UserRepository userRepository,
            AiCallLogRepository aiCallLogRepository,
            UserApiKeyRepository userApiKeyRepository) {
        this.userRepository = userRepository;
        this.aiCallLogRepository = aiCallLogRepository;
        this.userApiKeyRepository = userApiKeyRepository;
    }

    /**
     * 按可选 userId / apiKeyId 列出调用日志。
     */
    @Transactional(readOnly = true)
    public List<AdminAiCallResponse> list(Long operatorUserId, Long userId, Long apiKeyId) {
        log.info(
                "[后台CMS] listAiCalls start operatorUserId={}, userId={}, apiKeyId={}",
                operatorUserId,
                userId,
                apiKeyId);
        requireAdmin(operatorUserId);
        List<AiCallLogEntity> rows;
        if (userId != null && apiKeyId != null) {
            rows = aiCallLogRepository.findByUserIdAndApiKeyIdOrderByCreatedAtDesc(userId, apiKeyId);
        } else if (userId != null) {
            rows = aiCallLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else if (apiKeyId != null) {
            rows = aiCallLogRepository.findByApiKeyIdOrderByCreatedAtDesc(apiKeyId);
        } else {
            rows = aiCallLogRepository.findAllByOrderByCreatedAtDesc();
        }
        Set<Long> keyIds = rows.stream()
                .map(AiCallLogEntity::getApiKeyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        // 一次批量加载 key 掩码
        Map<Long, String> maskById = new HashMap<>();
        if (!keyIds.isEmpty()) {
            for (UserApiKeyEntity key : userApiKeyRepository.findAllById(keyIds)) {
                maskById.put(key.getId(), key.getKeyMask());
            }
        }
        List<AdminAiCallResponse> list = rows.stream()
                .map(row -> new AdminAiCallResponse(
                        row.getId(),
                        row.getUserId(),
                        row.getApiKeyId(),
                        row.getApiKeyId() == null ? null : maskById.get(row.getApiKeyId()),
                        row.getPurpose(),
                        row.getStatus(),
                        row.getShareToken(),
                        row.getCreatedAt()))
                .toList();
        log.info("[后台CMS] listAiCalls done size={}", list.size());
        return list;
    }

    private UserEntity requireAdmin(Long operatorUserId) {
        UserEntity operator = userRepository
                .findById(operatorUserId)
                .orElseThrow(() -> new ForbiddenException("无管理员权限"));
        if (operator.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("无管理员权限");
        }
        return operator;
    }
}
