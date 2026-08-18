package com.workout.modules.admin.api;

import com.workout.common.ApiRequest;
import com.workout.common.ApiResponse;
import com.workout.modules.admin.application.AdminApiKeyService;
import com.workout.modules.auth.api.CurrentUser;
import com.workout.modules.auth.domain.AuthPrincipal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CMS API Key HTTP 入口（接口层）。
 * ADMIN only；单用户与批量改写。
 */
@RestController
@RequestMapping("/api/v1/admin/apiKeys")
public class AdminApiKeyController {

    private static final Logger log = LoggerFactory.getLogger(AdminApiKeyController.class);

    private final AdminApiKeyService adminApiKeyService;

    /**
     * 注入服务。
     */
    public AdminApiKeyController(AdminApiKeyService adminApiKeyService) {
        this.adminApiKeyService = adminApiKeyService;
    }

    /**
     * 列出用户 key 掩码。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list() {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[后台CMS] listApiKeysHttp start operatorUserId={}", principal.getUserId());
        List<AdminApiKeyResponse> list = adminApiKeyService.list(principal.getUserId());
        log.info("[后台CMS] listApiKeysHttp done size={}", list.size());
        return ApiResponse.ok(Map.of("list", list));
    }

    /**
     * 列出密钥库（仅掩码）。
     */
    @GetMapping("/pool")
    public ApiResponse<Map<String, Object>> listPool() {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[后台CMS] listPoolHttp start operatorUserId={}", principal.getUserId());
        List<AdminApiKeyPoolResponse> list = adminApiKeyService.listPool(principal.getUserId());
        log.info("[后台CMS] listPoolHttp done size={}", list.size());
        return ApiResponse.ok(Map.of("list", list));
    }

    /**
     * 向密钥库新增 Key。
     */
    @PostMapping("/pool")
    public ApiResponse<AdminApiKeyPoolResponse> createPool(@RequestBody ApiRequest<AdminApiKeyUpsertRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        AdminApiKeyUpsertRequest req = body.getRequest();
        log.info("[后台CMS] createPoolHttp start operatorUserId={}", principal.getUserId());
        AdminApiKeyPoolResponse data =
                adminApiKeyService.createPool(principal.getUserId(), req == null ? null : req.getApiKey());
        log.info("[后台CMS] createPoolHttp done poolId={}, keyMask={}", data.getId(), data.getKeyMask());
        return ApiResponse.ok(data);
    }

    /**
     * 单用户改 key。
     */
    @PutMapping("/{userId}")
    public ApiResponse<AdminApiKeyResponse> upsert(
            @PathVariable Long userId, @RequestBody ApiRequest<AdminApiKeyUpsertRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        AdminApiKeyUpsertRequest req = body.getRequest();
        log.info("[后台CMS] upsertApiKeyHttp start operatorUserId={}, userId={}", principal.getUserId(), userId);
        AdminApiKeyResponse data =
                adminApiKeyService.upsert(principal.getUserId(), userId, req == null ? null : req.getApiKey());
        log.info("[后台CMS] upsertApiKeyHttp done userId={}, keyMask={}", userId, data.getKeyMask());
        return ApiResponse.ok(data);
    }

    /**
     * 批量改 key。
     */
    @PutMapping("/batch")
    public ApiResponse<Map<String, Object>> upsertBatch(@RequestBody ApiRequest<AdminApiKeyBatchRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        AdminApiKeyBatchRequest req = body.getRequest();
        log.info(
                "[后台CMS] upsertApiKeyBatchHttp start operatorUserId={}, size={}",
                principal.getUserId(),
                req == null || req.getUserIds() == null ? 0 : req.getUserIds().size());
        List<AdminApiKeyResponse> list = adminApiKeyService.upsertBatch(
                principal.getUserId(),
                req == null ? null : req.getUserIds(),
                req == null ? null : req.getApiKey());
        log.info("[后台CMS] upsertApiKeyBatchHttp done size={}", list.size());
        return ApiResponse.ok(Map.of("list", list));
    }
}
