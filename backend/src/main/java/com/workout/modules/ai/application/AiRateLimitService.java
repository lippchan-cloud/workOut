package com.workout.modules.ai.application;

import com.workout.modules.ai.infrastructure.AiCallLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * API Key 限流服务（应用层）。
 * 权威闸门：MySQL `work_out_ai_call_log` 按 api_key_id + 时间窗口 SQL COUNT。
 * 禁止 Redis / Caffeine / Guava 内存计数作为唯一闸门。
 */
@Service
public class AiRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(AiRateLimitService.class);
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    public static final int HOUR_LIMIT = 10;
    public static final int DAY_LIMIT = 100;

    private final AiCallLogRepository aiCallLogRepository;
    private final Clock clock;

    /**
     * 注入日志仓储与时钟（可测）。
     */
    public AiRateLimitService(AiCallLogRepository aiCallLogRepository, Clock clock) {
        this.aiCallLogRepository = aiCallLogRepository;
        this.clock = clock;
    }

    /**
     * 判断该 apiKey 是否仍可调用。
     *
     * @return true 表示未超限
     */
    public boolean allow(Long apiKeyId) {
        log.info("[AI限流] allow start apiKeyId={}", apiKeyId);
        Instant now = clock.instant();
        LocalDateTime shanghaiNow = LocalDateTime.ofInstant(now, SHANGHAI);
        Instant hourStart = shanghaiNow.withMinute(0).withSecond(0).withNano(0).atZone(SHANGHAI).toInstant();
        LocalDate day = shanghaiNow.toLocalDate();
        Instant dayStart = day.atStartOfDay(SHANGHAI).toInstant();
        // SQL 聚合计数，禁止拉日志到内存
        long hourCount = aiCallLogRepository.countByApiKeyIdSince(apiKeyId, hourStart);
        long dayCount = aiCallLogRepository.countByApiKeyIdSince(apiKeyId, dayStart);
        boolean ok = hourCount < HOUR_LIMIT && dayCount < DAY_LIMIT;
        log.info(
                "[AI限流] allow done apiKeyId={}, hourCount={}, dayCount={}, ok={}",
                apiKeyId,
                hourCount,
                dayCount,
                ok);
        return ok;
    }
}
