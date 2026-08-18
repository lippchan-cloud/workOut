package com.workout.modules.profile.application;

import com.workout.common.BusinessException;
import com.workout.modules.profile.api.ProfileRequest;
import com.workout.modules.profile.api.ProfileResponse;
import com.workout.modules.profile.infrastructure.ProfileEntity;
import com.workout.modules.profile.infrastructure.ProfileRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户资料应用服务（应用层）。
 * 仅操作 JWT 中的 userId，禁止信任客户端身份。
 */
@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final BigDecimal HEIGHT_MIN = new BigDecimal("50.0");
    private static final BigDecimal HEIGHT_MAX = new BigDecimal("250.0");
    private static final BigDecimal WEIGHT_MIN = new BigDecimal("20.0");
    private static final BigDecimal WEIGHT_MAX = new BigDecimal("300.0");

    private final ProfileRepository profileRepository;

    /**
     * 注入仓储。
     */
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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
     * 保存当前用户资料（upsert）。
     */
    @Transactional
    public ProfileResponse upsert(Long userId, ProfileRequest request) {
        long startMs = System.currentTimeMillis();
        log.info(
                "[资料] upsert start userId={}, nicknameLen={}, heightCm={}, weightKg={}",
                userId,
                request.getNickname() == null ? 0 : request.getNickname().length(),
                request.getHeightCm(),
                request.getWeightKg());
        validate(request);
        // 按 userId 加载已有行，避免为同一用户插入多条
        ProfileEntity entity = profileRepository.findByUserId(userId).orElseGet(ProfileEntity::new);
        entity.setUserId(userId);
        entity.setNickname(trimToNull(request.getNickname()));
        entity.setHeightCm(request.getHeightCm());
        entity.setWeightKg(request.getWeightKg());
        entity.setUpdatedAt(Instant.now());
        ProfileEntity saved = profileRepository.save(entity);
        log.info(
                "[资料] upsert done entityType=ProfileEntity id={}, userId={}, elapsedMs={}",
                saved.getId(),
                saved.getUserId(),
                System.currentTimeMillis() - startMs);
        return ProfileResponse.from(saved);
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
