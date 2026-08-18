package com.workout.modules.admin.api;

import com.workout.common.ApiResponse;
import com.workout.modules.admin.application.AdminAiCallService;
import com.workout.modules.auth.api.CurrentUser;
import com.workout.modules.auth.domain.AuthPrincipal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CMS AI 调用情况 HTTP 入口（接口层）。
 * ADMIN only；可按 userId、apiKeyId 筛选。
 */
@RestController
@RequestMapping("/api/v1/admin/aiCalls")
public class AdminAiCallController {

    private static final Logger log = LoggerFactory.getLogger(AdminAiCallController.class);

    private final AdminAiCallService adminAiCallService;

    /**
     * 注入服务。
     */
    public AdminAiCallController(AdminAiCallService adminAiCallService) {
        this.adminAiCallService = adminAiCallService;
    }

    /**
     * 列出调用日志。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) Long userId, @RequestParam(required = false) Long apiKeyId) {
        AuthPrincipal principal = CurrentUser.require();
        log.info(
                "[后台CMS] listAiCallsHttp start operatorUserId={}, userId={}, apiKeyId={}",
                principal.getUserId(),
                userId,
                apiKeyId);
        List<AdminAiCallResponse> list = adminAiCallService.list(principal.getUserId(), userId, apiKeyId);
        log.info("[后台CMS] listAiCallsHttp done size={}", list.size());
        return ApiResponse.ok(Map.of("list", list));
    }
}
