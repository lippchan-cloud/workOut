package com.workout.modules.profile.application;

import com.workout.common.BusinessException;
import com.workout.modules.profile.api.ProfileHistoryItemResponse;
import com.workout.modules.profile.api.ProfileRequest;
import com.workout.modules.profile.api.ProfileResponse;
import com.workout.modules.profile.api.ProfileTrendsResponse;
import com.workout.modules.profile.api.RecordCountResponse;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryRepository;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import com.workout.modules.record.infrastructure.DailyRecordRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户资料应用服务（应用层）。
 * 仅操作 JWT 中的 userId，禁止信任客户端身份；变更时写历史快照。
 */
@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal HEIGHT_MIN = new BigDecimal("50.0");
    private static final BigDecimal HEIGHT_MAX = new BigDecimal("250.0");
    private static final BigDecimal WEIGHT_MIN = new BigDecimal("20.0");
    private static final BigDecimal WEIGHT_MAX = new BigDecimal("300.0");

    private final ProfileRepository profileRepository;
    private final ProfileHistoryRepository profileHistoryRepository;
    private final DailyRecordRepository dailyRecordRepository;

    /**
     * 注入当前资料、历史与日记录仓储（条数一次查出）。
     */
    public ProfileService(
            ProfileRepository profileRepository,
            ProfileHistoryRepository profileHistoryRepository,
            DailyRecordRepository dailyRecordRepository) {
        this.profileRepository = profileRepository;
        this.profileHistoryRepository = profileHistoryRepository;
        this.dailyRecordRepository = dailyRecordRepository;
    }

    /**
     * 读取当前用户资料；尚无记录则返回空字段。
     */
    @Transactional(readOnly = true)
    public ProfileResponse get(Long userId) {
        log.info("[资料] get start userId={}", userId);
        ProfileResponse data = profileRepository.findByUserId(userId).map(ProfileResponse::from).orElseGet(ProfileResponse::empty);
        log.info("[资料] get done userId={}, hasNickname={}", userId, data.getNickname() != null);
        return data;
    }

    /**
     * 保存当前用户资料（upsert）；字段变化时追加历史快照。
     */
    @Transactional
    public ProfileResponse upsert(Long userId, ProfileRequest request) {
        long startMs = System.currentTimeMillis();
        log.info(
                "[资料] upsert start userId={}, nicknameLen={}, heightCm={}, weightKg={}, changedAt={}",
                userId,
                request.getNickname() == null ? 0 : request.getNickname().length(),
                request.getHeightCm(),
                request.getWeightKg(),
                request.getChangedAt());
        validate(request);
        Instant changedAt = resolveChangedAt(request.getChangedAt());
        String nickname = trimToNull(request.getNickname());
        // 按 userId 加载已有行，避免为同一用户插入多条
        ProfileEntity entity = profileRepository.findByUserId(userId).orElseGet(ProfileEntity::new);
        if (entity.getId() != null && snapshotEquals(entity.getNickname(), entity.getHeightCm(), entity.getWeightKg(), nickname, request.getHeightCm(), request.getWeightKg())) {
            log.info(
                    "[资料] upsert skip unchanged entityType=ProfileEntity id={}, userId={}, elapsedMs={}",
                    entity.getId(),
                    entity.getUserId(),
                    System.currentTimeMillis() - startMs);
            return ProfileResponse.from(entity);
        }
        entity.setUserId(userId);
        entity.setNickname(nickname);
        entity.setHeightCm(request.getHeightCm());
        entity.setWeightKg(request.getWeightKg());
        // 当前资料更新时间与历史快照使用同一真实日期
        entity.setUpdatedAt(changedAt);
        ProfileEntity saved = profileRepository.save(entity);
        // 字段相对最新快照有变化才追加，避免重复行
        appendHistoryIfChanged(saved, changedAt);
        log.info(
                "[资料] upsert done entityType=ProfileEntity id={}, userId={}, elapsedMs={}",
                saved.getId(),
                saved.getUserId(),
                System.currentTimeMillis() - startMs);
        return ProfileResponse.from(saved);
    }

    /**
     * 读取当前用户身体历史与按日记录条数，供变化曲线；各一次查询。
     */
    @Transactional(readOnly = true)
    public ProfileTrendsResponse trends(Long userId) {
        log.info("[资料] trends start userId={}", userId);
        // 一次加载该用户全部快照，禁止按日循环
        List<ProfileHistoryEntity> history = profileHistoryRepository.findByUserIdOrderByChangedAtAscIdAsc(userId);
        List<ProfileHistoryItemResponse> bodyHistory = history.stream()
                .map(row -> new ProfileHistoryItemResponse(
                        row.getChangedAt(), row.getNickname(), row.getHeightCm(), row.getWeightKg()))
                .toList();
        // 一次加载 recordedAt 再内存按上海自然日聚合，禁止 N+1
        List<Instant> recordedAts = dailyRecordRepository.findRecordedAtByUserIdAndDeletedFalse(userId);
        Map<String, Long> countByDate = new TreeMap<>();
        for (Instant recordedAt : recordedAts) {
            String date = recordedAt.atZone(SHANGHAI).toLocalDate().toString();
            countByDate.merge(date, 1L, Long::sum);
        }
        List<RecordCountResponse> recordCounts = countByDate.entrySet().stream()
                .map(entry -> new RecordCountResponse(entry.getKey(), entry.getValue()))
                .toList();
        log.info(
                "[资料] trends done userId={}, historySize={}, countDays={}, sampleHistoryIds={}",
                userId,
                history.size(),
                recordCounts.size(),
                history.stream().limit(20).map(ProfileHistoryEntity::getId).toList());
        return new ProfileTrendsResponse(bodyHistory, recordCounts);
    }

    /**
     * 相对最新快照有差异则插入一行变更后快照；changedAt 用资料真实日期。
     */
    private void appendHistoryIfChanged(ProfileEntity saved, Instant changedAt) {
        ProfileHistoryEntity latest = profileHistoryRepository
                .findTopByUserIdOrderByChangedAtDescIdDesc(saved.getUserId())
                .orElse(null);
        if (latest != null
                && snapshotEquals(
                        latest.getNickname(),
                        latest.getHeightCm(),
                        latest.getWeightKg(),
                        saved.getNickname(),
                        saved.getHeightCm(),
                        saved.getWeightKg())) {
            return;
        }
        ProfileHistoryEntity row = new ProfileHistoryEntity();
        row.setUserId(saved.getUserId());
        row.setChangedAt(changedAt);
        row.setNickname(saved.getNickname());
        row.setHeightCm(saved.getHeightCm());
        row.setWeightKg(saved.getWeightKg());
        ProfileHistoryEntity persisted = profileHistoryRepository.save(row);
        log.info(
                "[资料] history appended entityType=ProfileHistoryEntity id={}, userId={}",
                persisted.getId(),
                persisted.getUserId());
    }

    /**
     * 昵称与身高体重是否同一快照（身高体重用 compareTo，避免 scale 差异）。
     */
    private boolean snapshotEquals(
            String leftNickname,
            BigDecimal leftHeight,
            BigDecimal leftWeight,
            String rightNickname,
            BigDecimal rightHeight,
            BigDecimal rightWeight) {
        return Objects.equals(leftNickname, rightNickname)
                && sameDecimal(leftHeight, rightHeight)
                && sameDecimal(leftWeight, rightWeight);
    }

    /**
     * 可空 BigDecimal 数值相等。
     */
    private boolean sameDecimal(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    /**
     * 解析资料真实日期：请求有则用，缺省此刻；不允许明显未来。
     */
    private Instant resolveChangedAt(Instant requested) {
        Instant now = Instant.now();
        if (requested == null) {
            return now;
        }
        if (requested.isAfter(now.plus(Duration.ofMinutes(5)))) {
            throw new BusinessException("资料真实日期不能是未来时间");
        }
        return requested;
    }

    /**
     * 校验身高体重区间与昵称长度。
     */
    private void validate(ProfileRequest request) {
        if (request.getNickname() != null && request.getNickname().length() > 32) {
            throw new BusinessException("昵称最多32字");
        }
        if (request.getHeightCm() != null
                && (request.getHeightCm().compareTo(HEIGHT_MIN) < 0
                        || request.getHeightCm().compareTo(HEIGHT_MAX) > 0)) {
            throw new BusinessException("身高需在 50～250 厘米之间");
        }
        if (request.getWeightKg() != null
                && (request.getWeightKg().compareTo(WEIGHT_MIN) < 0
                        || request.getWeightKg().compareTo(WEIGHT_MAX) > 0)) {
            throw new BusinessException("体重需在 20～300 千克之间");
        }
    }

    /**
     * 空白昵称存为 null。
     */
    private String trimToNull(String nickname) {
        if (nickname == null) {
            return null;
        }
        String trimmed = nickname.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
