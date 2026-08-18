package com.workout.modules.auth.api;

import com.workout.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 受保护桩接口（接口层）。
 * 仅用于 JWT 过滤器契约测试，不承载业务数据。
 */
@RestController
@RequestMapping("/api/v1/secure")
public class SecurePingController {

    private static final Logger log = LoggerFactory.getLogger(SecurePingController.class);

    /**
     * 已认证探测：返回固定 pong。
     */
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        log.info("[鉴权探测] ping start");
        log.info("[鉴权探测] ping done data=pong");
        return ApiResponse.ok("pong");
    }
}
