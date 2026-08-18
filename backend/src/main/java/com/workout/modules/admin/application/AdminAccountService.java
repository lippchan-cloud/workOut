package com.workout.modules.admin.application;

import com.workout.common.ForbiddenException;
import com.workout.common.NotFoundException;
import com.workout.modules.admin.api.AdminAccountResponse;
import com.workout.modules.admin.api.AdminRecentRecordResponse;
import com.workout.modules.admin.api.AdminShareListItemResponse;
import com.workout.modules.admin.api.AdminUserDetailResponse;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import com.workout.modules.record.infrastructure.DailyRecordEntity;
import com.workout.modules.record.infrastructure.DailyRecordRepository;
import com.workout.modules.share.infrastructure.ShareReportEntity;
import com.workout.modules.share.infrastructure.ShareReportRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台 CMS 应用服务（应用层）。
 * 账户列表、用户详情、已有分享列表；一次批量查询，禁止 N+1。CMS 调用方必须为 ADMIN。
 */
@Service
public class AdminAccountService {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountService.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ShareReportRepository shareReportRepository;

    /**
     * 注入用户、资料、记录与分享仓储。
     */
    public AdminAccountService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            DailyRecordRepository dailyRecordRepository,
            ShareReportRepository shareReportRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.shareReportRepository = shareReportRepository;
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
        requireAdmin(operatorUserId);
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
                users.stream().map(user -> toAccountResponse(user, profileByUserId.get(user.getId()))).toList();
        log.info(
                "[后台CMS] listAll done size={}, profileHits={}, elapsedMs={}",
                list.size(),
                profiles.size(),
                System.currentTimeMillis() - startMs);
        return list;
    }

    /**
     * 校验 ADMIN 后返回指定用户详情：资料、最近记录摘要、已有分享链接。
     *
     * @param operatorUserId JWT 中的管理员主键
     * @param userId         被查看用户主键（路径参数，非身份）
     * @return 用户详情
     */
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getDetail(Long operatorUserId, Long userId) {
        long startMs = System.currentTimeMillis();
        log.info("[后台CMS] getDetail start operatorUserId={}, userId={}", operatorUserId, userId);
        requireAdmin(operatorUserId);
        // 按主键一次加载目标用户；不存在则 404，禁止把缺失当空资料
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        log.info(
                "[后台CMS] loaded entityType=UserEntity id={}, username={}, role={}",
                user.getId(),
                user.getUsername(),
                user.getRole());
        // 一次加载资料，无资料时可见字段为 null
        ProfileEntity profile = profileRepository.findByUserId(userId).orElse(null);
        // 一次统计条数 + 一次取最近 5 条，禁止循环单查
        long recordCount = dailyRecordRepository.countByUserIdAndDeletedFalse(userId);
        List<DailyRecordEntity> recent = dailyRecordRepository
                .findTop5ByUserIdAndDeletedFalseOrderByRecordedAtDescIdDesc(userId);
        log.info("[后台CMS] loaded entityType=DailyRecordEntity userId={}, recordCount={}, recentSize={}",
                userId, recordCount, recent.size());
        List<AdminRecentRecordResponse> recentRecords = recent.stream()
                .map(row -> new AdminRecentRecordResponse(
                        row.getId(), row.getType(), row.getContent(), row.getRecordedAt()))
                .toList();
        // 只读该用户已有分享，不代为生成
        List<ShareReportEntity> shares = shareReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("[后台CMS] loaded entityType=ShareReportEntity userId={}, shareSize={}", userId, shares.size());
        List<AdminShareListItemResponse> shareItems = shares.stream()
                .map(row -> new AdminShareListItemResponse(
                        row.getToken(),
                        row.getUserId(),
                        user.getUsername(),
                        row.getRangeFrom(),
                        row.getRangeTo(),
                        row.getCreatedAt()))
                .toList();
        AdminUserDetailResponse response = new AdminUserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getRole(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getHeightCm(),
                profile == null ? null : profile.getWeightKg(),
                recordCount,
                recentRecords,
                shareItems);
        log.info(
                "[后台CMS] getDetail done userId={}, recordCount={}, shareSize={}, elapsedMs={}",
                userId,
                recordCount,
                shareItems.size(),
                System.currentTimeMillis() - startMs);
        return response;
    }

    /**
     * 校验 ADMIN 后列出全站已有分享；用户名批量拼装，禁止按行查用户。
     *
     * @param operatorUserId JWT 中的管理员主键
     * @return 分享列表
     */
    @Transactional(readOnly = true)
    public List<AdminShareListItemResponse> listReports(Long operatorUserId) {
        long startMs = System.currentTimeMillis();
        log.info("[后台CMS] listReports start operatorUserId={}", operatorUserId);
        requireAdmin(operatorUserId);
        // 一次加载全部已有分享，CMS 不创建新快照
        List<ShareReportEntity> shares = shareReportRepository.findAllByOrderByCreatedAtDesc();
        log.info(
                "[后台CMS] loaded entityType=ShareReportEntity size={}, sampleTokens={}",
                shares.size(),
                shares.stream().limit(20).map(row -> prefix(row.getToken())).toList());
        if (shares.isEmpty()) {
            log.info("[后台CMS] listReports done size=0 elapsedMs={}", System.currentTimeMillis() - startMs);
            return List.of();
        }
        List<Long> ownerIds = shares.stream().map(ShareReportEntity::getUserId).distinct().toList();
        // 批量加载用户名，禁止 for-loop findById
        Map<Long, UserEntity> usersById = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
        List<AdminShareListItemResponse> list = shares.stream()
                .map(row -> {
                    UserEntity owner = usersById.get(row.getUserId());
                    return new AdminShareListItemResponse(
                            row.getToken(),
                            row.getUserId(),
                            owner == null ? null : owner.getUsername(),
                            row.getRangeFrom(),
                            row.getRangeTo(),
                            row.getCreatedAt());
                })
                .toList();
        log.info(
                "[后台CMS] listReports done size={}, userHits={}, elapsedMs={}",
                list.size(),
                usersById.size(),
                System.currentTimeMillis() - startMs);
        return list;
    }

    /**
     * 按主键加载操作者并校验 ADMIN，角色以库为准。
     */
    private UserEntity requireAdmin(Long operatorUserId) {
        UserEntity operator = userRepository
                .findById(operatorUserId)
                .orElseThrow(() -> new ForbiddenException("无管理员权限"));
        log.info(
                "[后台CMS] loaded operator entityType=UserEntity id={}, username={}, role={}",
                operator.getId(),
                operator.getUsername(),
                operator.getRole());
        if (operator.getRole() != UserRole.ADMIN) {
            log.error("[后台CMS] denied code=403 operatorUserId={}, role={}", operatorUserId, operator.getRole());
            throw new ForbiddenException("无管理员权限");
        }
        return operator;
    }

    /**
     * 将用户与可选资料转为 API 视图；无资料时可见字段为 null。
     */
    private AdminAccountResponse toAccountResponse(UserEntity user, ProfileEntity profile) {
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

    /**
     * token 日志脱敏：前 8 位。
     */
    private String prefix(String token) {
        if (token == null || token.length() < 8) {
            return "...";
        }
        return token.substring(0, 8) + "...";
    }
}
