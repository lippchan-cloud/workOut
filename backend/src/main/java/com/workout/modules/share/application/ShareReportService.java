package com.workout.modules.share.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.common.NotFoundException;
import com.workout.config.WorkoutPublicProperties;
import com.workout.modules.ai.application.ShareAdviceRequestedEvent;
import com.workout.modules.ai.application.ShareAdviceService;
import com.workout.modules.ai.domain.AdviceStatus;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryEntity;
import com.workout.modules.profile.infrastructure.ProfileHistoryRepository;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import com.workout.modules.record.api.DailyRecordResponse;
import com.workout.modules.record.application.DailyRecordService;
import com.workout.modules.record.domain.RecordQueryPeriod;
import com.workout.modules.share.api.MyShareListItemResponse;
import com.workout.modules.share.api.ShareCreateResponse;
import com.workout.modules.share.api.ShareSnapshotResponse;
import com.workout.modules.share.api.ShareSnapshotResponse.ShareBodyPoint;
import com.workout.modules.share.api.ShareSnapshotResponse.ShareRecordItem;
import com.workout.modules.share.infrastructure.ShareReportEntity;
import com.workout.modules.share.infrastructure.ShareReportRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分享报告应用服务（应用层）。
 * 创建时冻结快照并异步触发建议；公开读取只按 token，不信任客户端 userId。
 */
@Service
public class ShareReportService {

    private static final Logger log = LoggerFactory.getLogger(ShareReportService.class);

    private final DailyRecordService dailyRecordService;
    private final ProfileRepository profileRepository;
    private final ProfileHistoryRepository profileHistoryRepository;
    private final UserRepository userRepository;
    private final ShareReportRepository shareReportRepository;
    private final UserApiKeyRepository userApiKeyRepository;
    private final WorkoutPublicProperties publicProperties;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 注入记录、资料、用户、分享与 AI 触发依赖。
     */
    public ShareReportService(
            DailyRecordService dailyRecordService,
            ProfileRepository profileRepository,
            ProfileHistoryRepository profileHistoryRepository,
            UserRepository userRepository,
            ShareReportRepository shareReportRepository,
            UserApiKeyRepository userApiKeyRepository,
            WorkoutPublicProperties publicProperties,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {
        this.dailyRecordService = dailyRecordService;
        this.profileRepository = profileRepository;
        this.profileHistoryRepository = profileHistoryRepository;
        this.userRepository = userRepository;
        this.shareReportRepository = shareReportRepository;
        this.userApiKeyRepository = userApiKeyRepository;
        this.publicProperties = publicProperties;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 按当前筛选创建只读分享；须已填身高体重；有 key 则 PENDING 并异步生成建议。
     */
    @Transactional
    public ShareCreateResponse create(
            Long userId, LocalDate date, YearMonth yearMonth, LocalDate from, LocalDate to) {
        long startMs = System.currentTimeMillis();
        log.info(
                "[分享] create start userId={}, date={}, yearMonth={}, from={}, to={}",
                userId,
                date,
                yearMonth,
                from,
                to);
        // 与导出共用闸门，缺身高体重不得落库
        dailyRecordService.requireCompleteBody(userId);
        RecordQueryPeriod period = dailyRecordService.resolvePeriod(date, yearMonth, from, to);
        List<DailyRecordResponse> records = dailyRecordService.listByPeriod(userId, period);
        // 一次加载历史，禁止按事项循环查库
        List<ProfileHistoryEntity> history =
                profileHistoryRepository.findByUserIdOrderByChangedAtAscIdAsc(userId);
        ProfileEntity profile = profileRepository.findByUserId(userId).orElse(null);
        UserEntity user = userRepository.findById(userId).orElse(null);
        String displayName = resolveDisplayName(profile, user);
        boolean hasKey = userApiKeyRepository.findByUserId(userId).isPresent();
        AdviceStatus initialStatus = hasKey ? AdviceStatus.PENDING : AdviceStatus.NONE_KEY;
        ShareSnapshotResponse snapshot = buildSnapshot(period, displayName, records, history, initialStatus);
        String token = UUID.randomUUID().toString().replace("-", "");
        ShareReportEntity row = new ShareReportEntity();
        row.setToken(token);
        row.setUserId(userId);
        row.setRangeFrom(period.getFrom());
        row.setRangeTo(period.getTo());
        row.setSnapshotJson(writeJson(snapshot));
        row.setAdviceStatus(initialStatus.name());
        row.setCreatedAt(Instant.now());
        ShareReportEntity saved = shareReportRepository.save(row);
        if (hasKey) {
            // 提交后异步生成，不阻塞本 HTTP
            eventPublisher.publishEvent(new ShareAdviceRequestedEvent(token, userId));
        }
        String url = publicProperties.getPublicBaseUrl() + "/report/" + token;
        log.info(
                "[分享] create done entityType=ShareReportEntity id={}, userId={}, adviceStatus={}, tokenPrefix={}, rows={}, elapsedMs={}",
                saved.getId(),
                saved.getUserId(),
                initialStatus,
                token.substring(0, Math.min(8, token.length())) + "...",
                records.size(),
                System.currentTimeMillis() - startMs);
        return new ShareCreateResponse(token, url);
    }

    /**
     * 按公开 token 读取冻结快照，无需登录。
     */
    @Transactional(readOnly = true)
    public ShareSnapshotResponse getPublic(String token) {
        log.info("[分享] getPublic start tokenPrefix={}", token == null ? "" : token.substring(0, Math.min(8, token.length())));
        ShareReportEntity row = shareReportRepository
                .findByToken(token)
                .orElseThrow(() -> {
                    log.error("[分享] getPublic failed code=404 tokenMissing=true");
                    return new NotFoundException("报告不存在");
                });
        log.info(
                "[分享] getPublic loaded entityType=ShareReportEntity id={}, userId={}, adviceStatus={}",
                row.getId(),
                row.getUserId(),
                row.getAdviceStatus());
        try {
            ShareSnapshotResponse snapshot = objectMapper.readValue(row.getSnapshotJson(), ShareSnapshotResponse.class);
            // 以列状态为准，兼容旧快照缺字段
            String status = row.getAdviceStatus() == null ? AdviceStatus.NONE_KEY.name() : row.getAdviceStatus();
            snapshot.setAdviceStatus(status);
            if (AdviceStatus.NONE_KEY.name().equals(status)
                    && (snapshot.getAdvice() == null || snapshot.getAdvice().isBlank())) {
                snapshot.setAdvice(ShareAdviceService.MSG_NO_KEY);
            }
            if (AdviceStatus.PENDING.name().equals(status) && snapshot.getAdvice() == null) {
                snapshot.setAdvice(null);
            }
            log.info(
                    "[分享] getPublic done tokenPrefix={}, from={}, to={}, adviceStatus={}",
                    token.substring(0, 8),
                    snapshot.getFrom(),
                    snapshot.getTo(),
                    status);
            return snapshot;
        } catch (JsonProcessingException ex) {
            log.error("[分享] getPublic failed code=500 parseSnapshot id={}", row.getId());
            throw new IllegalStateException("报告快照损坏", ex);
        }
    }

    /**
     * 列出当前用户已有分享，按创建时间倒序；一次查询，禁止循环。
     *
     * @param userId JWT 用户主键
     * @return 公开 token 与范围摘要
     */
    @Transactional(readOnly = true)
    public List<MyShareListItemResponse> listMine(Long userId) {
        log.info("[分享] listMine start userId={}", userId);
        List<ShareReportEntity> rows = shareReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<MyShareListItemResponse> list = rows.stream()
                .map(row -> new MyShareListItemResponse(
                        row.getToken(), row.getRangeFrom(), row.getRangeTo(), row.getCreatedAt()))
                .toList();
        log.info("[分享] listMine done userId={}, size={}", userId, list.size());
        return list;
    }

    /**
     * 显示名优先昵称，否则用户名。
     */
    private String resolveDisplayName(ProfileEntity profile, UserEntity user) {
        if (profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()) {
            return profile.getNickname();
        }
        return user == null ? "" : user.getUsername();
    }

    /**
     * 组装冻结快照：范围、显示名、事项、曲线点与初始建议状态。
     */
    private ShareSnapshotResponse buildSnapshot(
            RecordQueryPeriod period,
            String displayName,
            List<DailyRecordResponse> records,
            List<ProfileHistoryEntity> history,
            AdviceStatus adviceStatus) {
        ShareSnapshotResponse snapshot = new ShareSnapshotResponse();
        snapshot.setFrom(period.getFrom().toString());
        snapshot.setTo(period.getTo().toString());
        snapshot.setDisplayName(displayName);
        snapshot.setRecords(records.stream()
                .map(row -> {
                    ShareRecordItem item = new ShareRecordItem();
                    item.setRecordedAt(row.getRecordedAt());
                    item.setType(row.getType().name());
                    item.setContent(row.getContent());
                    return item;
                })
                .toList());
        snapshot.setBodyHistory(history.stream()
                .map(row -> {
                    ShareBodyPoint point = new ShareBodyPoint();
                    point.setChangedAt(row.getChangedAt());
                    point.setHeightCm(row.getHeightCm());
                    point.setWeightKg(row.getWeightKg());
                    return point;
                })
                .toList());
        snapshot.setAdviceStatus(adviceStatus.name());
        if (adviceStatus == AdviceStatus.NONE_KEY) {
            snapshot.setAdvice(ShareAdviceService.MSG_NO_KEY);
        } else {
            snapshot.setAdvice(null);
        }
        return snapshot;
    }

    /**
     * 将快照写成 JSON 文本。
     */
    private String writeJson(ShareSnapshotResponse snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("无法序列化分享快照", ex);
        }
    }
}
