package com.workout.record;

import com.workout.auth.ApiRequest;
import com.workout.auth.AuthPrincipal;
import com.workout.auth.CurrentUser;
import com.workout.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日记录 HTTP 入口（接口层）。
 * 身份一律取自 JWT，忽略客户端可能传入的 userId。
 */
@RestController
@RequestMapping("/api/v1/dailyRecords")
public class DailyRecordController {

    private static final Logger log = LoggerFactory.getLogger(DailyRecordController.class);

    private final DailyRecordService dailyRecordService;

    /**
     * 注入日记录服务。
     */
    public DailyRecordController(DailyRecordService dailyRecordService) {
        this.dailyRecordService = dailyRecordService;
    }

    /**
     * 创建消耗/摄入记录。
     */
    @PostMapping
    public ApiResponse<DailyRecordResponse> create(@Valid @RequestBody ApiRequest<CreateDailyRecordRequest> body) {
        // 从 JWT 解析当前用户，禁止信任 body 中的身份
        AuthPrincipal principal = CurrentUser.require();
        CreateDailyRecordRequest request = body.getRequest();
        log.info(
                "[日记录] DailyRecordController.create start userId={}, type={}",
                principal.getUserId(),
                request.getType());
        // 委托应用服务校验并落库
        DailyRecordResponse data = dailyRecordService.create(principal.getUserId(), request);
        log.info("[日记录] DailyRecordController.create done id={}, userId={}", data.getId(), principal.getUserId());
        return ApiResponse.ok(data);
    }

    /**
     * 按日查询当前用户记录列表。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[日记录] DailyRecordController.listByDate start userId={}, date={}", principal.getUserId(), date);
        // 按用户隔离查询当日列表
        List<DailyRecordResponse> list = dailyRecordService.listByDate(principal.getUserId(), date);
        log.info(
                "[日记录] DailyRecordController.listByDate done userId={}, date={}, size={}",
                principal.getUserId(),
                date,
                list.size());
        return ApiResponse.ok(Map.of("date", date.toString(), "list", list));
    }

    /**
     * 按日导出当前用户 CSV（UTF-8 BOM）。
     */
    @GetMapping("/exportCsv")
    public void exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response)
            throws IOException {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[日记录] DailyRecordController.exportCsv start userId={}, date={}", principal.getUserId(), date);
        byte[] csv = dailyRecordService.exportCsv(principal.getUserId(), date);
        String filename = "workout-" + date + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(csv);
        log.info("[日记录] DailyRecordController.exportCsv done userId={}, date={}, bytes={}",
                principal.getUserId(), date, csv.length);
    }
}
