package com.workout.common;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 HTTP 入口（横切）。
 * 用于锁定统一信封形态，不承载业务数据。
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    /**
     * 返回简单 OK 载荷，供信封契约测试使用。
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        log.info("[健康检查] health start");
        ApiResponse<Map<String, String>> response = ApiResponse.ok(Map.of("status", "UP"));
        log.info("[健康检查] health done status=UP");
        return response;
    }
}
