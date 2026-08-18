package com.workout.modules.admin.api;

import com.workout.common.ApiResponse;
import com.workout.modules.admin.application.AdminAccountService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 CMS 账户列表 HTTP 入口（接口层）。
 * TEMPORARY：第一阶段无需登录即可访问，后续必须加鉴权；仅只读列表，不返回密码哈希。
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
     * 列出全部账户（临时公开）。
     */
    @GetMapping("/accounts")
    public ApiResponse<Map<String, Object>> listAccounts() {
        // 关键入口：明确记录临时无鉴权，便于后续收口
        log.info("[后台CMS] listAccounts start temporaryUnauthenticated=true");
        // 委托应用服务批量装配用户与资料
        List<AdminAccountResponse> list = adminAccountService.listAll();
        log.info("[后台CMS] listAccounts done size={}", list.size());
        return ApiResponse.ok(Map.of("list", list));
    }
}
