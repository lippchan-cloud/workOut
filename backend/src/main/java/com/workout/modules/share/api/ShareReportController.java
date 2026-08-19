package com.workout.modules.share.api;

import com.workout.common.ApiResponse;
import com.workout.modules.auth.api.CurrentUser;
import com.workout.modules.auth.domain.AuthPrincipal;
import com.workout.modules.share.application.ShareReportService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享创建 HTTP 入口（接口层）。
 * 身份取自 JWT；筛选参数与日历导出一致。
 */
@RestController
@RequestMapping("/api/v1/shareReports")
public class ShareReportController {

    private static final Logger log = LoggerFactory.getLogger(ShareReportController.class);

    private final ShareReportService shareReportService;

    /**
     * 注入分享服务。
     */
    public ShareReportController(ShareReportService shareReportService) {
        this.shareReportService = shareReportService;
    }

    /**
     * 列出当前用户已创建的分享报告。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> listMine() {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[分享] ShareReportController.listMine start userId={}", principal.getUserId());
        List<MyShareListItemResponse> list = shareReportService.listMine(principal.getUserId());
        log.info("[分享] ShareReportController.listMine done userId={}, size={}", principal.getUserId(), list.size());
        return ApiResponse.ok(Map.of("list", list));
    }

    /**
     * 为当前筛选创建只读分享链接。
     */
    @PostMapping
    public ApiResponse<ShareCreateResponse> create(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AuthPrincipal principal = CurrentUser.require();
        log.info(
                "[分享] ShareReportController.create start userId={}, date={}, yearMonth={}, from={}, to={}",
                principal.getUserId(),
                date,
                yearMonth,
                from,
                to);
        ShareCreateResponse data =
                shareReportService.create(principal.getUserId(), date, yearMonth, from, to);
        log.info("[分享] ShareReportController.create done userId={}, idPrefix={}", principal.getUserId(), data.getId().substring(0, 8));
        return ApiResponse.ok(data);
    }
}
