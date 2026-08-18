package com.workout.modules.auth.application;

import com.workout.common.BusinessException;
import com.workout.config.AdminProperties;
import com.workout.modules.auth.api.AuthTokenResponse;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.JwtService;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.profile.infrastructure.ProfileHistoryRepository;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import com.workout.modules.record.infrastructure.DailyRecordRepository;
import com.workout.modules.share.infrastructure.ShareReportRepository;
import com.workout.modules.ai.application.ApiKeyAssignmentService;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务（应用层）。
 * 负责注册与登录编排：唯一性校验、密码哈希核对、落库、签发 JWT；不暴露仓储细节给 Controller。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminProperties adminProperties;
    private final DailyRecordRepository dailyRecordRepository;
    private final ProfileRepository profileRepository;
    private final ProfileHistoryRepository profileHistoryRepository;
    private final ShareReportRepository shareReportRepository;
    private final ApiKeyAssignmentService apiKeyAssignmentService;
    private final UserApiKeyRepository userApiKeyRepository;

    /**
     * 注入注册、改密与注销所需依赖。
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AdminProperties adminProperties,
            DailyRecordRepository dailyRecordRepository,
            ProfileRepository profileRepository,
            ProfileHistoryRepository profileHistoryRepository,
            ShareReportRepository shareReportRepository,
            ApiKeyAssignmentService apiKeyAssignmentService,
            UserApiKeyRepository userApiKeyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.adminProperties = adminProperties;
        this.dailyRecordRepository = dailyRecordRepository;
        this.profileRepository = profileRepository;
        this.profileHistoryRepository = profileHistoryRepository;
        this.shareReportRepository = shareReportRepository;
        this.apiKeyAssignmentService = apiKeyAssignmentService;
        this.userApiKeyRepository = userApiKeyRepository;
    }

    /**
     * 注册新用户并立即签发 Token。
     *
     * @param username 唯一用户名
     * @param rawPassword 明文密码（不会落库）
     * @return token 与用户标识
     */
    @Transactional
    public AuthTokenResponse register(String username, String rawPassword) {
        long startMs = System.currentTimeMillis();
        // 关键入口：只记录用户名，禁止打印明文密码
        log.info("[鉴权注册] register start username={}", username);
        // 业务规则：用户名全站唯一
        if (userRepository.existsByUsername(username)) {
            log.error("[鉴权注册] register failed code=400 msg=该用户名已被注册 username={}", username);
            throw new BusinessException("该用户名已被注册");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username.trim());
        // 明文转 BCrypt 哈希后再持久化
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(Instant.now());
        // 引导名单内的用户名提升为 ADMIN，其余默认 USER
        user.setRole(resolveRole(user.getUsername()));
        // 写入用户行并回填自增主键
        UserEntity saved = userRepository.save(user);
        // 关键实体：落库后的用户标识与角色
        log.info(
                "[鉴权注册] saved entityType=UserEntity id={}, username={}, role={}",
                saved.getId(),
                saved.getUsername(),
                saved.getRole());

        // 新用户默认从密钥库取一把关联
        apiKeyAssignmentService.assignDefaultIfAbsent(saved.getId());

        // 注册成功即发 JWT，避免再调一次登录
        String token = jwtService.issueToken(saved.getId(), saved.getUsername());
        log.info(
                "[鉴权注册] register done success=true userId={}, role={}, elapsedMs={}",
                saved.getId(),
                saved.getRole(),
                System.currentTimeMillis() - startMs);
        return new AuthTokenResponse(token, saved.getId(), saved.getUsername(), saved.getRole());
    }

    /**
     * 校验用户名与密码并签发 Token。
     * 用户不存在与密码错误使用同一文案，避免泄露账号是否已注册。
     *
     * @param username 登录名
     * @param rawPassword 明文密码（不会落库）
     * @return token 与用户标识
     */
    @Transactional
    public AuthTokenResponse login(String username, String rawPassword) {
        long startMs = System.currentTimeMillis();
        // 关键入口：只记录用户名，禁止打印明文密码
        log.info("[鉴权登录] login start username={}", username);
        // 按用户名加载用户；找不到与密码错误走同一失败文案
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.error("[鉴权登录] login failed code=400 msg=用户名或密码错误 username={}", username);
            throw new BusinessException("用户名或密码错误");
        }
        // 关键实体：核对通过后的用户标识
        log.info("[鉴权登录] loaded entityType=UserEntity id={}, username={}, role={}",
                user.getId(), user.getUsername(), user.getRole());
        // 登录时按引导名单纠偏角色，避免漏升管理员
        UserRole resolved = resolveRole(user.getUsername());
        if (resolved == UserRole.ADMIN && user.getRole() != UserRole.ADMIN) {
            user.setRole(UserRole.ADMIN);
            // 仅提升、不降级，登录后 CMS 立即生效
            userRepository.save(user);
            log.info("[鉴权登录] promoted entityType=UserEntity id={}, role={}", user.getId(), user.getRole());
        }
        // 登录成功签发 JWT
        String token = jwtService.issueToken(user.getId(), user.getUsername());
        log.info(
                "[鉴权登录] login done success=true userId={}, role={}, elapsedMs={}",
                user.getId(),
                user.getRole(),
                System.currentTimeMillis() - startMs);
        return new AuthTokenResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 校验当前密码后写入新哈希。
     *
     * @param userId          JWT 用户主键
     * @param currentPassword 当前明文密码
     * @param newPassword     新明文密码
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        long startMs = System.currentTimeMillis();
        log.info("[鉴权改密] changePassword start userId={}", userId);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户名或密码错误"));
        log.info("[鉴权改密] loaded entityType=UserEntity id={}, username={}", user.getId(), user.getUsername());
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.error("[鉴权改密] changePassword failed code=400 msg=当前密码不正确 userId={}", userId);
            throw new BusinessException("当前密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info(
                "[鉴权改密] changePassword done userId={}, elapsedMs={}", userId, System.currentTimeMillis() - startMs);
    }

    /**
     * 注销当前用户：批量删除本人记录与资料后再删用户行。
     *
     * @param userId JWT 用户主键
     */
    @Transactional
    public void deleteMe(Long userId) {
        long startMs = System.currentTimeMillis();
        log.info("[鉴权注销] deleteMe start userId={}", userId);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        log.info("[鉴权注销] loaded entityType=UserEntity id={}, username={}", user.getId(), user.getUsername());
        // 先按 userId 批量删从属数据，禁止循环 deleteById
        dailyRecordRepository.deleteByUserId(userId);
        profileHistoryRepository.deleteByUserId(userId);
        log.info("[鉴权注销] deleted profile history userId={}", userId);
        shareReportRepository.deleteByUserId(userId);
        log.info("[鉴权注销] deleted share reports userId={}", userId);
        userApiKeyRepository.deleteByUserId(userId);
        log.info("[鉴权注销] deleted api key binding userId={}", userId);
        profileRepository.deleteByUserId(userId);
        userRepository.delete(user);
        log.info("[鉴权注销] deleteMe done userId={}, elapsedMs={}", userId, System.currentTimeMillis() - startMs);
    }

    /**
     * 按引导配置解析角色；名单外一律 USER。
     */
    private UserRole resolveRole(String username) {
        // 对照 yml 引导名单，不查询其它权限表
        return adminProperties.isBootstrapAdmin(username) ? UserRole.ADMIN : UserRole.USER;
    }
}
