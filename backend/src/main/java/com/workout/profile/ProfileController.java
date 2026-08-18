package com.workout.profile;

import com.workout.auth.ApiRequest;
import com.workout.auth.AuthPrincipal;
import com.workout.auth.CurrentUser;
import com.workout.common.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料 HTTP 入口（接口层）。
 * 身份取自 JWT，忽略客户端 userId。
 */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    /**
     * 注入资料服务。
     */
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 读取当前用户资料。
     */
    @GetMapping
    public ApiResponse<ProfileResponse> get() {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[资料] ProfileController.get start userId={}", principal.getUserId());
        ProfileResponse data = profileService.get(principal.getUserId());
        log.info("[资料] ProfileController.get done userId={}", principal.getUserId());
        return ApiResponse.ok(data);
    }

    /**
     * 保存当前用户资料。
     */
    @PutMapping
    public ApiResponse<ProfileResponse> put(@Valid @RequestBody ApiRequest<ProfileRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[资料] ProfileController.put start userId={}", principal.getUserId());
        ProfileResponse data = profileService.upsert(principal.getUserId(), body.getRequest());
        log.info("[资料] ProfileController.put done userId={}, idLoaded=true", principal.getUserId());
        return ApiResponse.ok(data);
    }
}
