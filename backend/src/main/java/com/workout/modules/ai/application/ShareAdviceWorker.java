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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 建议生成写库 Worker（应用层）。
 * 独立 Bean + REQUIRES_NEW，保证异步线程内一定有事务。
 */
@Service
public class ShareAdviceWorker {

    private static final Logger log = LoggerFactory.getLogger(ShareAdviceWorker.class);

    private final ShareReportRepository shareReportRepository;
    private final UserApiKeyRepository userApiKeyRepository;
    private final AiCallLogRepository aiCallLogRepository;
    private final AiRateLimitService rateLimitService;
    private final AiContextCompressService compressService;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    /**
     * 注入依赖。
     */
    public ShareAdviceWorker(
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
     * 在新事务中生成建议。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateInNewTx(String token, Long expectedUserId) {
        log.info(
                "[建议分析] generateInNewTx start tokenPrefix={}, expectedUserId={}",
                tokenPrefix(token),
                expectedUserId);
        ShareReportEntity row = shareReportRepository
                .findByToken(token)
                .orElseThrow(() -> new IllegalStateException("share missing"));
        if (!row.getUserId().equals(expectedUserId)) {
            throw new IllegalStateException("userId boundary violation");
        }
        if (AdviceStatus.NONE_KEY.name().equals(row.getAdviceStatus())) {
            log.info("[建议分析] skip NONE_KEY tokenPrefix={}", tokenPrefix(token));
            return;
        }
        UserApiKeyEntity key = userApiKeyRepository.findByUserId(row.getUserId()).orElse(null);
        if (key == null) {
            updateAdvice(row, AdviceStatus.NONE_KEY, ShareAdviceService.MSG_NO_KEY);
            writeLog(row.getUserId(), null, token, "SKIPPED");
            return;
        }
        if (!rateLimitService.allow(rateKeyId(key))) {
            updateAdvice(row, AdviceStatus.FAILED, ShareAdviceService.MSG_RATE);
            writeLog(row.getUserId(), rateKeyId(key), token, "RATE_LIMITED");
            return;
        }
        try {
            ShareSnapshotResponse snapshot = objectMapper.readValue(row.getSnapshotJson(), ShareSnapshotResponse.class);
            String compressed = compressService.compressAndStore(row.getUserId(), snapshot, token);
            if (!compressed.contains("userId=" + row.getUserId())) {
                throw new IllegalStateException("compressed context missing userId");
            }
            // 把同用户历史压缩询问一并塞进 prompt，受字数上限约束
            String withHistory = compressService.assembleWithHistory(row.getUserId(), compressed);
            String advice = deepSeekClient.chat(
                    key.getApiKey(),
                    PhysioScientistPrompts.SYSTEM,
                    PhysioScientistPrompts.userMessage(row.getUserId(), withHistory));
            updateAdvice(row, AdviceStatus.READY, advice);
            writeLog(row.getUserId(), rateKeyId(key), token, "SUCCESS");
            log.info(
                    "[建议分析] generateInNewTx done entityType=ShareReportEntity id={}, status=READY",
                    row.getId());
        } catch (Exception ex) {
            log.error(
                    "[建议分析] generateInNewTx model/path failed tokenPrefix={}, msg={}",
                    tokenPrefix(token),
                    ex.getMessage());
            updateAdvice(row, AdviceStatus.FAILED, ShareAdviceService.MSG_FAIL);
            writeLog(row.getUserId(), rateKeyId(key), token, "FAILED");
        }
    }

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

    /**
     * 限流与调用日志按密钥库 id；缺 poolId 时回退绑定行 id。
     */
    private static Long rateKeyId(UserApiKeyEntity key) {
        return key.getPoolId() != null ? key.getPoolId() : key.getId();
    }

    private static String tokenPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.substring(0, Math.min(8, token.length())) + "...";
    }
}
