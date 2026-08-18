package com.workout.modules.admin.application;

import com.workout.common.BusinessException;
import com.workout.common.ForbiddenException;
import com.workout.common.NotFoundException;
import com.workout.modules.admin.api.AdminApiKeyPoolResponse;
import com.workout.modules.admin.api.AdminApiKeyResponse;
import com.workout.modules.ai.application.ApiKeyAssignmentService;
import com.workout.modules.ai.infrastructure.ApiKeyPoolEntity;
import com.workout.modules.ai.infrastructure.ApiKeyPoolRepository;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CMS API Key 管理（应用层）。
 * 单用户 / 批量改写；响应仅掩码；禁止日志打印完整 key。
 */
@Service
public class AdminApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(AdminApiKeyService.class);

    private final UserRepository userRepository;
    private final UserApiKeyRepository userApiKeyRepository;
    private final ApiKeyPoolRepository apiKeyPoolRepository;
    private final ApiKeyAssignmentService apiKeyAssignmentService;

    /**
     * 注入用户、绑定、密钥库与分配服务。
     */
    public AdminApiKeyService(
            UserRepository userRepository,
            UserApiKeyRepository userApiKeyRepository,
            ApiKeyPoolRepository apiKeyPoolRepository,
            ApiKeyAssignmentService apiKeyAssignmentService) {
        this.userRepository = userRepository;
        this.userApiKeyRepository = userApiKeyRepository;
        this.apiKeyPoolRepository = apiKeyPoolRepository;
        this.apiKeyAssignmentService = apiKeyAssignmentService;
    }

    /**
     * 列出全部用户及其 key 掩码（无 key 则 mask 为空）。
     */
    @Transactional(readOnly = true)
    public List<AdminApiKeyResponse> list(Long operatorUserId) {
        log.info("[后台CMS] listApiKeys start operatorUserId={}", operatorUserId);
        requireAdmin(operatorUserId);
        List<UserEntity> users = userRepository.findAll();
        List<Long> ids = users.stream().map(UserEntity::getId).toList();
        // 一次 IN 查询，禁止按用户循环查 key
        Map<Long, UserApiKeyEntity> byUser = userApiKeyRepository.findByUserIdIn(ids).stream()
                .collect(Collectors.toMap(UserApiKeyEntity::getUserId, e -> e, (a, b) -> a));
        List<AdminApiKeyResponse> list = users.stream()
                .map(u -> {
                    UserApiKeyEntity key = byUser.get(u.getId());
                    return new AdminApiKeyResponse(
                            u.getId(),
                            u.getUsername(),
                            key == null ? null : key.getPoolId(),
                            key == null ? null : key.getKeyMask(),
                            key != null);
                })
                .toList();
        log.info("[后台CMS] listApiKeys done size={}", list.size());
        return list;
    }

    /**
     * 单用户设置 / 覆盖 API Key。
     */
    @Transactional
    public AdminApiKeyResponse upsert(Long operatorUserId, Long userId, String apiKey) {
        log.info("[后台CMS] upsertApiKey start operatorUserId={}, userId={}, keyMask={}", operatorUserId, userId, mask(apiKey));
        requireAdmin(operatorUserId);
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("apiKey 不能为空");
        }
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("用户不存在"));
        ApiKeyPoolEntity pool = apiKeyAssignmentService.upsertPool(apiKey.trim(), operatorUserId);
        UserApiKeyEntity saved = apiKeyAssignmentService.bind(userId, pool, operatorUserId);
        log.info(
                "[后台CMS] upsertApiKey done entityType=UserApiKeyEntity id={}, userId={}, poolId={}, keyMask={}",
                saved.getId(),
                userId,
                saved.getPoolId(),
                saved.getKeyMask());
        return new AdminApiKeyResponse(user.getId(), user.getUsername(), saved.getPoolId(), saved.getKeyMask(), true);
    }

    /**
     * 批量为多个用户设置同一 API Key。
     */
    @Transactional
    public List<AdminApiKeyResponse> upsertBatch(Long operatorUserId, List<Long> userIds, String apiKey) {
        log.info(
                "[后台CMS] upsertApiKeyBatch start operatorUserId={}, size={}, keyMask={}",
                operatorUserId,
                userIds == null ? 0 : userIds.size(),
                mask(apiKey));
        requireAdmin(operatorUserId);
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("apiKey 不能为空");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("userIds 不能为空");
        }
        Set<Long> idSet = new HashSet<>(userIds);
        List<UserEntity> users = userRepository.findAllById(idSet);
        if (users.size() != idSet.size()) {
            throw new NotFoundException("部分用户不存在");
        }
        Map<Long, UserEntity> userMap = users.stream().collect(Collectors.toMap(UserEntity::getId, u -> u));
        ApiKeyPoolEntity pool = apiKeyAssignmentService.upsertPool(apiKey.trim(), operatorUserId);
        Map<Long, UserApiKeyEntity> existing = userApiKeyRepository.findByUserIdIn(idSet).stream()
                .collect(Collectors.toMap(UserApiKeyEntity::getUserId, e -> e, (a, b) -> a));
        java.time.Instant now = java.time.Instant.now();
        List<UserApiKeyEntity> toSave = new ArrayList<>();
        for (Long uid : idSet) {
            UserApiKeyEntity row = existing.getOrDefault(uid, new UserApiKeyEntity());
            row.setUserId(uid);
            row.setPoolId(pool.getId());
            row.setApiKey(pool.getApiKey());
            row.setKeyMask(pool.getKeyMask());
            row.setUpdatedAt(now);
            row.setUpdatedBy(operatorUserId);
            toSave.add(row);
        }
        List<UserApiKeyEntity> saved = userApiKeyRepository.saveAll(toSave);
        Map<Long, UserApiKeyEntity> savedByUser =
                saved.stream().collect(Collectors.toMap(UserApiKeyEntity::getUserId, e -> e, (a, b) -> a));
        List<AdminApiKeyResponse> result = idSet.stream()
                .map(uid -> {
                    UserEntity u = userMap.get(uid);
                    UserApiKeyEntity k = savedByUser.get(uid);
                    return new AdminApiKeyResponse(uid, u.getUsername(), k.getPoolId(), k.getKeyMask(), true);
                })
                .toList();
        log.info("[后台CMS] upsertApiKeyBatch done size={}", result.size());
        return result;
    }

    /**
     * 列出密钥库（仅掩码）。
     */
    @Transactional(readOnly = true)
    public List<AdminApiKeyPoolResponse> listPool(Long operatorUserId) {
        log.info("[后台CMS] listPool start operatorUserId={}", operatorUserId);
        requireAdmin(operatorUserId);
        List<AdminApiKeyPoolResponse> list = apiKeyPoolRepository.findAll().stream()
                .map(p -> new AdminApiKeyPoolResponse(p.getId(), p.getKeyMask(), p.isEnabled()))
                .toList();
        log.info("[后台CMS] listPool done size={}", list.size());
        return list;
    }

    /**
     * 向密钥库新增一把 Key。
     */
    @Transactional
    public AdminApiKeyPoolResponse createPool(Long operatorUserId, String apiKey) {
        log.info("[后台CMS] createPool start operatorUserId={}, keyMask={}", operatorUserId, mask(apiKey));
        requireAdmin(operatorUserId);
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("apiKey 不能为空");
        }
        ApiKeyPoolEntity saved = apiKeyAssignmentService.upsertPool(apiKey.trim(), operatorUserId);
        log.info(
                "[后台CMS] createPool done entityType=ApiKeyPoolEntity id={}, keyMask={}",
                saved.getId(),
                saved.getKeyMask());
        return new AdminApiKeyPoolResponse(saved.getId(), saved.getKeyMask(), saved.isEnabled());
    }

    /**
     * 掩码：**** + 尾 4 位。
     */
    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
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
