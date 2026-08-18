package com.workout.common;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal health endpoint used to lock the API envelope shape in tests.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * Return a simple OK payload wrapped in the standard envelope.
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        // Expose a stable probe for bootstrap and envelope contract tests.
        return ApiResponse.ok(Map.of("status", "UP"));
    }
}
