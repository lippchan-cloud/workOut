package com.workout.modules.admin.application;

import com.workout.common.ForbiddenException;
import com.workout.modules.admin.api.AdminAccountResponse;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台账户列表应用服务（应用层）。
 * 一次加载全部用户、一次按 userId 批量加载资料，禁止 N+1。CMS 调用方必须为 ADMIN。
 */
@Service
public class AdminAccountService {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountService.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * 注入用户与资料仓储。
     */
    public AdminAccountService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * 校验当前用户为 ADMIN 后列出全部账户及可见资料；不含密码哈希。
     *
     * @param operatorUserId JWT 中的当前用户主键
     * @return 账户列表
     */
    @Transactional(readOnly = true)
    public List<AdminAccountResponse> listAll(Long operatorUserId) {
        long startMs = System.currentTimeMillis();
        log.info("[后台CMS] listAll start operatorUserId={}", operatorUserId);
        // 按主键一次加载操作者，校验 ADMIN，禁止信任客户端角色
        UserEntity operator = userRepository
                .findById(operatorUserId)
                .orElseThrow(() -> new ForbiddenException("无管理员权限"));
        log.info(
                "[后台CMS] loaded operator entityType=UserEntity id={}, username={}, role={}",
                operator.getId(),
                operator.getUsername(),
                operator.getRole());
        if (operator.getRole() != UserRole.ADMIN) {
            log.error("[后台CMS] listAll denied code=403 operatorUserId={}, role={}", operatorUserId, operator.getRole());
            throw new ForbiddenException("无管理员权限");
        }
        // 一次查出全部用户，按创建时间倒序便于运营查看新注册
        List<UserEntity> users = userRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        log.info(
                "[后台CMS] loaded entityType=UserEntity size={}, sampleIds={}",
                users.size(),
                users.stream().limit(20).map(UserEntity::getId).toList());
        if (users.isEmpty()) {
            log.info("[后台CMS] listAll done size=0 elapsedMs={}", System.currentTimeMillis() - startMs);
            return List.of();
        }
        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        // 批量加载资料，禁止 for-loop 按 userId 查库
        List<ProfileEntity> profiles = profileRepository.findByUserIdIn(userIds);
        Map<Long, ProfileEntity> profileByUserId = profiles.stream()
                .collect(Collectors.toMap(ProfileEntity::getUserId, profile -> profile, (left, right) -> left));
        List<AdminAccountResponse> list =
                users.stream().map(user -> toResponse(user, profileByUserId.get(user.getId()))).toList();
        log.info(
                "[后台CMS] listAll done size={}, profileHits={}, elapsedMs={}",
                list.size(),
                profiles.size(),
                System.currentTimeMillis() - startMs);
        return list;
    }

    /**
     * 将用户与可选资料转为 API 视图；无资料时可见字段为 null。
     */
    private AdminAccountResponse toResponse(UserEntity user, ProfileEntity profile) {
        if (profile == null) {
            return new AdminAccountResponse(
                    user.getId(), user.getUsername(), user.getCreatedAt(), user.getRole(), null, null, null);
        }
        return new AdminAccountResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getRole(),
                profile.getNickname(),
                profile.getHeightCm(),
                profile.getWeightKg());
    }
}
