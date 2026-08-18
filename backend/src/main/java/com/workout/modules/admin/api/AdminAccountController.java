package com.workout.modules.admin.api;

import com.workout.common.ApiResponse;
import com.workout.modules.admin.application.AdminAccountService;
import com.workout.modules.auth.api.CurrentUser;
import com.workout.modules.auth.domain.AuthPrincipal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 CMS 账户列表 HTTP 入口（接口层）。
 * 必须已登录且角色为 ADMIN；仅只读列表，不返回密码哈希。
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminAccountController {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountController.class);

    private final AdminAccountService adminAccountService;

    /**
     * 注入账户列表应用服务。
     */
    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    /**
     * 列出全部账户；身份取自 JWT。
     */
    @GetMapping("/accounts")
    public ApiResponse<Map<String, Object>> listAccounts() {
        // 从 JWT 取操作者，禁止信任 query/body 中的身份
        AuthPrincipal principal = CurrentUser.require();
        log.info("[后台CMS] listAccounts start operatorUserId={}", principal.getUserId());
        // 委托应用服务校验 ADMIN 并批量装配用户与资料
        List<AdminAccountResponse> list = adminAccountService.listAll(principal.getUserId());
        log.info("[后台CMS] listAccounts done operatorUserId={}, size={}", principal.getUserId(), list.size());
        return ApiResponse.ok(Map.of("list", list));
    }
}
