package com.workout.modules.share.api;

import com.workout.common.ApiResponse;
import com.workout.modules.share.application.ShareReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开报告 HTTP 入口（接口层）。
 * 无需登录；只按 token 返回冻结快照。
 */
@RestController
@RequestMapping("/api/v1/reports")
public class PublicReportController {

    private static final Logger log = LoggerFactory.getLogger(PublicReportController.class);

    private final ShareReportService shareReportService;

    /**
     * 注入分享服务。
     */
    public PublicReportController(ShareReportService shareReportService) {
        this.shareReportService = shareReportService;
    }

    /**
     * 匿名读取分享报告。
     */
    @GetMapping("/{id}")
    public ApiResponse<ShareSnapshotResponse> get(@PathVariable String id) {
        log.info("[分享] PublicReportController.get start idPrefix={}", id == null ? "" : id.substring(0, Math.min(8, id.length())));
        ShareSnapshotResponse data = shareReportService.getPublic(id);
        log.info("[分享] PublicReportController.get done displayNameLen={}", data.getDisplayName() == null ? 0 : data.getDisplayName().length());
        return ApiResponse.ok(data);
    }
}
