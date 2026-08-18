package com.workout.modules.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.modules.ai.domain.AdviceStatus;
import com.workout.modules.ai.domain.AiCallPurpose;
import com.workout.modules.ai.infrastructure.AiCallLogEntity;
import com.workout.modules.ai.infrastructure.AiCallLogRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.share.api.ShareSnapshotResponse;
import com.workout.modules.share.infrastructure.ShareReportEntity;
import com.workout.modules.share.infrastructure.ShareReportRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 分享建议异步生成（应用层）。
 * AFTER_COMMIT 触发；失败只更新报告状态，不回滚分享。
 */
@Service
public class ShareAdviceService {

    private static final Logger log = LoggerFactory.getLogger(ShareAdviceService.class);
    public static final String MSG_NO_KEY = "未配置 API Key";
    public static final String MSG_RATE = "调用过于频繁，请稍后再试（仅供参考占位）";
    public static final String MSG_FAIL = "建议生成失败，请稍后重试（仅供参考）";

    private final ShareReportRepository shareReportRepository;
    private final UserApiKeyRepository userApiKeyRepository;
    private final AiCallLogRepository aiCallLogRepository;
    private final AiRateLimitService rateLimitService;
    private final AiContextCompressService compressService;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    /**
     * 注入分享、key、限流、压缩与模型客户端。
     */
    public ShareAdviceService(
            ShareReportRepository shareReportRepository,
            UserApiKeyRepository userApiKeyRepository,
            AiCallLogRepository aiCallLogRepository,
            AiRateLimitService rateLimitService,
            AiContextCompressService compressService,
            DeepSeekClient deepSeekClient,
            ObjectMapper objectMapper) {
        this.shareReportRepository = shareReportRepository;
        this.userApiKeyRepository = userApiKeyRepository;
        this.aiCallLogRepository = aiCallLogRepository;
        this.rateLimitService = rateLimitService;
        this.compressService = compressService;
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 事务提交后异步生成建议。
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShareAdviceRequested(ShareAdviceRequestedEvent event) {
        log.info(
                "[建议分析] onShareAdviceRequested start tokenPrefix={}, userId={}",
                tokenPrefix(event.getShareToken()),
                event.getUserId());
        // 委托同步事务方法完成写库，隔离异常
        try {
            generateForToken(event.getShareToken(), event.getUserId());
        } catch (Exception ex) {
            log.error(
                    "[建议分析] onShareAdviceRequested failed tokenPrefix={}, msg={}",
                    tokenPrefix(event.getShareToken()),
                    ex.getMessage());
        }
    }

    /**
     * 按 token 生成建议（可测试入口）。
     */
    @Transactional
    public void generateForToken(String token, Long expectedUserId) {
        log.info("[建议分析] generateForToken start tokenPrefix={}, expectedUserId={}", tokenPrefix(token), expectedUserId);
        ShareReportEntity row = shareReportRepository
                .findByToken(token)
                .orElseThrow(() -> new IllegalStateException("share missing"));
        // 强制 userId 边界：事件与行必须一致
        if (!row.getUserId().equals(expectedUserId)) {
            log.error(
                    "[建议分析] userId mismatch rowUserId={}, expected={}",
                    row.getUserId(),
                    expectedUserId);
            throw new IllegalStateException("userId boundary violation");
        }
        if (AdviceStatus.NONE_KEY.name().equals(row.getAdviceStatus())) {
            log.info("[建议分析] skip NONE_KEY tokenPrefix={}", tokenPrefix(token));
            return;
        }
        UserApiKeyEntity key = userApiKeyRepository.findByUserId(row.getUserId()).orElse(null);
        if (key == null) {
            // 运行中 key 被删：降级
            updateAdvice(row, AdviceStatus.NONE_KEY, MSG_NO_KEY);
            writeLog(row.getUserId(), null, token, "SKIPPED");
            return;
        }
        if (!rateLimitService.allow(key.getId())) {
            updateAdvice(row, AdviceStatus.FAILED, MSG_RATE);
            // 一次尝试一条日志，避免重复计数
            writeLog(row.getUserId(), key.getId(), token, "RATE_LIMITED");
            return;
        }
        try {
            ShareSnapshotResponse snapshot = objectMapper.readValue(row.getSnapshotJson(), ShareSnapshotResponse.class);
            // 压缩仅该用户快照
            String compressed = compressService.compressAndStore(row.getUserId(), snapshot, token);
            if (!compressed.contains("userId=" + row.getUserId())) {
                throw new IllegalStateException("compressed context missing userId");
            }
            String advice = deepSeekClient.chat(
                    key.getApiKey(),
                    PhysioScientistPrompts.SYSTEM,
                    PhysioScientistPrompts.userMessage(row.getUserId(), compressed));
            updateAdvice(row, AdviceStatus.READY, advice);
            writeLog(row.getUserId(), key.getId(), token, "SUCCESS");
            log.info(
                    "[建议分析] generateForToken done entityType=ShareReportEntity id={}, status=READY",
                    row.getId());
        } catch (Exception ex) {
            log.error(
                    "[建议分析] generateForToken failed tokenPrefix={}, msg={}",
                    tokenPrefix(token),
                    ex.getMessage());
            updateAdvice(row, AdviceStatus.FAILED, MSG_FAIL);
            writeLog(row.getUserId(), key.getId(), token, "FAILED");
        }
    }

    /**
     * 写调用日志（限流与 CMS）。
     */
    private void writeLog(Long userId, Long apiKeyId, String token, String status) {
        AiCallLogEntity logRow = new AiCallLogEntity();
        logRow.setUserId(userId);
        logRow.setApiKeyId(apiKeyId);
        logRow.setPurpose(AiCallPurpose.SHARE_ADVICE.name());
        logRow.setStatus(status);
        logRow.setShareToken(token);
        logRow.setCreatedAt(Instant.now());
        AiCallLogEntity saved = aiCallLogRepository.save(logRow);
        log.info(
                "[建议分析] callLog entityType=AiCallLogEntity id={}, userId={}, apiKeyId={}, status={}",
                saved.getId(),
                userId,
                apiKeyId,
                status);
    }

    /**
     * 更新建议状态与快照内 advice 字段。
     */
    private void updateAdvice(ShareReportEntity row, AdviceStatus status, String adviceText) {
        try {
            ShareSnapshotResponse snapshot = objectMapper.readValue(row.getSnapshotJson(), ShareSnapshotResponse.class);
            snapshot.setAdvice(adviceText);
            snapshot.setAdviceStatus(status.name());
            row.setSnapshotJson(objectMapper.writeValueAsString(snapshot));
            row.setAdviceStatus(status.name());
            shareReportRepository.save(row);
            log.info(
                    "[建议分析] updated entityType=ShareReportEntity id={}, adviceStatus={}",
                    row.getId(),
                    status);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("无法更新建议快照", ex);
        }
    }

    private static String tokenPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.substring(0, Math.min(8, token.length())) + "...";
    }
}
