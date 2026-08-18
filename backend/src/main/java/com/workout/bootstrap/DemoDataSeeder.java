package com.workout.bootstrap;

import com.workout.modules.ai.application.ApiKeyAssignmentService;
import com.workout.modules.auth.domain.UserRole;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryRepository;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import com.workout.modules.record.infrastructure.DailyRecordEntity;
import com.workout.modules.record.infrastructure.DailyRecordRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 非 test 环境启动时写入 demo 演示账号与约上下三个月记录（启动层）。
 * 已存在 demo 用户则整次跳过；批量 saveAll，禁止按条查库。
 */
@Component
@Profile("!test")
@Order(200)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final ProfileHistoryRepository profileHistoryRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ApiKeyAssignmentService apiKeyAssignmentService;
    private final Clock clock;

    /**
     * 注入仓储与密码编码；Clock 缺省为上海系统时钟。
     */
    public DemoDataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileRepository profileRepository,
            ProfileHistoryRepository profileHistoryRepository,
            DailyRecordRepository dailyRecordRepository,
            ApiKeyAssignmentService apiKeyAssignmentService,
            ObjectProvider<Clock> clockProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileRepository = profileRepository;
        this.profileHistoryRepository = profileHistoryRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.apiKeyAssignmentService = apiKeyAssignmentService;
        this.clock = clockProvider.getIfAvailable(() -> Clock.system(SHANGHAI));
    }

    /**
     * 启动时尝试播种 demo 账号；已存在则跳过。
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long startMs = System.currentTimeMillis();
        // 关键入口：只记用户名，禁止打印明文密码
        log.info("[演示种子] run start username={}", DemoDataset.USERNAME);
        // 已有同名用户则整次跳过，避免重复插入
        if (userRepository.existsByUsername(DemoDataset.USERNAME)) {
            log.info("[演示种子] run skip existing username={}", DemoDataset.USERNAME);
            return;
        }
        // 按当前时钟生成约 ±90 天计划
        DemoDataset dataset = DemoDataset.build(clock);
        UserEntity user = new UserEntity();
        user.setUsername(DemoDataset.USERNAME);
        // 明文转 BCrypt 后再落库
        user.setPasswordHash(passwordEncoder.encode(DemoDataset.PASSWORD));
        user.setCreatedAt(Instant.now(clock));
        user.setRole(UserRole.USER);
        UserEntity saved = userRepository.save(user);
        log.info("[演示种子] saved entityType=UserEntity id={}, username={}", saved.getId(), saved.getUsername());

        ProfileEntity profile = new ProfileEntity();
        profile.setUserId(saved.getId());
        profile.setNickname(DemoDataset.NICKNAME);
        profile.setHeightCm(dataset.currentHeightCm());
        profile.setWeightKg(dataset.currentWeightKg());
        profile.setUpdatedAt(Instant.now(clock));
        // 当前资料一行，供导出/分享闸门
        ProfileEntity savedProfile = profileRepository.save(profile);
        log.info(
                "[演示种子] saved entityType=ProfileEntity id={}, userId={}",
                savedProfile.getId(),
                savedProfile.getUserId());

        List<ProfileHistoryEntity> historyRows = dataset.history().stream()
                .map(point -> toHistory(saved.getId(), point))
                .toList();
        // 一次批量写入历史，禁止循环 save
        profileHistoryRepository.saveAll(historyRows);

        List<DailyRecordEntity> recordRows = dataset.records().stream()
                .map(row -> toRecord(saved.getId(), row, clock))
                .toList();
        // 一次批量写入事项
        dailyRecordRepository.saveAll(recordRows);
        // 密钥库若已有启用 key，给 demo 默认绑定
        apiKeyAssignmentService.assignDefaultIfAbsent(saved.getId());
        log.info(
                "[演示种子] run done userId={}, records={}, history={}, elapsedMs={}",
                saved.getId(),
                recordRows.size(),
                historyRows.size(),
                System.currentTimeMillis() - startMs);
    }

    /**
     * 把计划历史映射为持久化实体。
     */
    private static ProfileHistoryEntity toHistory(Long userId, DemoDataset.PlannedHistory point) {
        ProfileHistoryEntity entity = new ProfileHistoryEntity();
        entity.setUserId(userId);
        entity.setChangedAt(point.changedAt());
        entity.setNickname(point.nickname());
        entity.setHeightCm(point.heightCm());
        entity.setWeightKg(point.weightKg());
        return entity;
    }

    /**
     * 把计划事项映射为持久化实体。
     */
    private static DailyRecordEntity toRecord(Long userId, DemoDataset.PlannedRecord row, Clock clock) {
        DailyRecordEntity entity = new DailyRecordEntity();
        entity.setUserId(userId);
        entity.setType(row.type());
        entity.setContent(row.content());
        entity.setRecordedAt(row.recordedAt());
        entity.setCreatedAt(Instant.now(clock));
        entity.setDeleted(false);
        return entity;
    }
}
