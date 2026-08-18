package com.workout.modules.record.api;

import com.workout.common.ApiRequest;
import com.workout.common.ApiResponse;
import com.workout.modules.auth.api.CurrentUser;
import com.workout.modules.auth.domain.AuthPrincipal;
import com.workout.modules.record.application.DailyRecordService;
import com.workout.modules.record.domain.RecordQueryPeriod;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
     * 更新当前用户自己的消耗/摄入记录。
     */
    @PutMapping("/{id}")
    public ApiResponse<DailyRecordResponse> update(
            @PathVariable Long id, @Valid @RequestBody ApiRequest<CreateDailyRecordRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        CreateDailyRecordRequest request = body.getRequest();
        log.info(
                "[日记录] DailyRecordController.update start userId={}, id={}, type={}",
                principal.getUserId(),
                id,
                request.getType());
        DailyRecordResponse data = dailyRecordService.update(principal.getUserId(), id, request);
        log.info("[日记录] DailyRecordController.update done id={}, userId={}", data.getId(), principal.getUserId());
        return ApiResponse.ok(data);
    }

    /**
     * 删除当前用户自己的记录（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[日记录] DailyRecordController.delete start userId={}, id={}", principal.getUserId(), id);
        dailyRecordService.delete(principal.getUserId(), id);
        log.info("[日记录] DailyRecordController.delete done id={}, userId={}", id, principal.getUserId());
        return ApiResponse.ok(null);
    }

    /**
     * 按 id 读取当前用户自己的一条记录（详情页刷新直达）。
     */
    @GetMapping("/{id}")
    public ApiResponse<DailyRecordResponse> getById(@PathVariable Long id) {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[日记录] DailyRecordController.getById start userId={}, id={}", principal.getUserId(), id);
        DailyRecordResponse data = dailyRecordService.getById(principal.getUserId(), id);
        log.info("[日记录] DailyRecordController.getById done id={}, userId={}", data.getId(), principal.getUserId());
        return ApiResponse.ok(data);
    }

    /**
     * 按单日 / 整月 / 自定义区间查询当前用户记录列表。参数互斥由应用服务解析。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AuthPrincipal principal = CurrentUser.require();
        log.info(
                "[日记录] DailyRecordController.list start userId={}, date={}, yearMonth={}, from={}, to={}",
                principal.getUserId(),
                date,
                yearMonth,
                from,
                to);
        // 解析筛选区间后再一次仓储查询，身份只取 JWT
        RecordQueryPeriod period = dailyRecordService.resolvePeriod(date, yearMonth, from, to);
        List<DailyRecordResponse> list = dailyRecordService.listByPeriod(principal.getUserId(), period);
        log.info(
                "[日记录] DailyRecordController.list done userId={}, from={}, to={}, size={}",
                principal.getUserId(),
                period.getFrom(),
                period.getTo(),
                list.size());
        return ApiResponse.ok(dailyRecordService.toListData(period, list));
    }

    /**
     * 按当前筛选导出当前用户 CSV（UTF-8 BOM）。
     */
    @GetMapping("/exportCsv")
    public void exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response)
            throws IOException {
        AuthPrincipal principal = CurrentUser.require();
        log.info(
                "[日记录] DailyRecordController.exportCsv start userId={}, date={}, yearMonth={}, from={}, to={}",
                principal.getUserId(),
                date,
                yearMonth,
                from,
                to);
        // 与列表共用 period 解析，保证筛选一致且带 JWT userId
        RecordQueryPeriod period = dailyRecordService.resolvePeriod(date, yearMonth, from, to);
        byte[] csv = dailyRecordService.exportCsv(principal.getUserId(), period);
        // 文件名随模式变化：整月 YYYY-MM，同日 YYYY-MM-DD，跨日用下划线
        String filename = period.csvFilename();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        // 写出带 BOM 的 CSV 字节
        response.getOutputStream().write(csv);
        log.info(
                "[日记录] DailyRecordController.exportCsv done userId={}, filename={}, bytes={}",
                principal.getUserId(),
                filename,
                csv.length);
    }
}
