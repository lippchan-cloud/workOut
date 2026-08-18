package com.workout.modules.record.application;

import com.workout.common.BusinessException;
import com.workout.common.NotFoundException;
import com.workout.modules.record.api.CreateDailyRecordRequest;
import com.workout.modules.record.api.DailyRecordResponse;
import com.workout.modules.record.domain.RecordQueryPeriod;
import com.workout.modules.record.domain.RecordType;
import com.workout.modules.record.infrastructure.DailyRecordEntity;
import com.workout.modules.record.infrastructure.DailyRecordRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日记录应用服务（应用层）。
 * 负责创建、按日/月/区间查询与导出；一律使用 JWT 中的 userId，禁止信任客户端身份字段。
 */
@Service
public class DailyRecordService {

    private static final Logger log = LoggerFactory.getLogger(DailyRecordService.class);
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final int MAX_CONTENT = 500;

    private final DailyRecordRepository dailyRecordRepository;

    /**
     * 注入仓储。
     */
    public DailyRecordService(DailyRecordRepository dailyRecordRepository) {
        this.dailyRecordRepository = dailyRecordRepository;
    }

    /**
     * 为当前用户创建一条日记录。
     *
     * @param userId  JWT 用户主键
     * @param request 创建请求
     * @return 新建记录
     */
    @Transactional
    public DailyRecordResponse create(Long userId, CreateDailyRecordRequest request) {
        long startMs = System.currentTimeMillis();
        // 关键入口：脱敏入参，不打印完整超长内容
        log.info(
                "[日记录] create start userId={}, type={}, contentLen={}, recordedAt={}",
                userId,
                request.getType(),
                request.getContent() == null ? 0 : request.getContent().length(),
                request.getRecordedAt());
        String content = normalizeContent(userId, request.getContent());

        DailyRecordEntity entity = new DailyRecordEntity();
        entity.setUserId(userId);
        entity.setType(request.getType());
        entity.setContent(content);
        entity.setRecordedAt(request.getRecordedAt());
        entity.setCreatedAt(Instant.now());
        // 落库并回填主键
        DailyRecordEntity saved = dailyRecordRepository.save(entity);
        // 关键实体：新建记录标识
        log.info(
                "[日记录] create done entityType=DailyRecordEntity id={}, userId={}, elapsedMs={}",
                saved.getId(),
                saved.getUserId(),
                System.currentTimeMillis() - startMs);
        return DailyRecordResponse.from(saved);
    }

    /**
     * 更新当前用户自己的日记录。
     *
     * @param userId  JWT 用户主键
     * @param id      记录主键
     * @param request 更新字段
     * @return 更新后的记录
     */
    @Transactional
    public DailyRecordResponse update(Long userId, Long id, CreateDailyRecordRequest request) {
        long startMs = System.currentTimeMillis();
        log.info(
                "[日记录] update start userId={}, id={}, type={}, contentLen={}",
                userId,
                id,
                request.getType(),
                request.getContent() == null ? 0 : request.getContent().length());
        DailyRecordEntity entity = requireOwned(userId, id);
        String content = normalizeContent(userId, request.getContent());
        entity.setType(request.getType());
        entity.setContent(content);
        entity.setRecordedAt(request.getRecordedAt());
        DailyRecordEntity saved = dailyRecordRepository.save(entity);
        log.info(
                "[日记录] update done entityType=DailyRecordEntity id={}, userId={}, elapsedMs={}",
                saved.getId(),
                saved.getUserId(),
                System.currentTimeMillis() - startMs);
        return DailyRecordResponse.from(saved);
    }

    /**
     * 按 id 读取当前用户自己的日记录，供详情页刷新直达。
     *
     * @param userId JWT 用户主键
     * @param id     记录主键
     * @return 未删除且属于该用户的记录
     */
    @Transactional(readOnly = true)
    public DailyRecordResponse getById(Long userId, Long id) {
        log.info("[日记录] getById start userId={}, id={}", userId, id);
        // 一次按 id+userId 加载，跨用户与缺失走同一 404
        DailyRecordEntity entity = requireOwned(userId, id);
        log.info(
                "[日记录] getById loaded entityType=DailyRecordEntity id={}, userId={}, type={}",
                entity.getId(),
                entity.getUserId(),
                entity.getType());
        return DailyRecordResponse.from(entity);
    }

    /**
     * 逻辑删除当前用户自己的日记录。
     *
     * @param userId JWT 用户主键
     * @param id     记录主键
     */
    @Transactional
    public void delete(Long userId, Long id) {
        long startMs = System.currentTimeMillis();
        log.info("[日记录] delete start userId={}, id={}", userId, id);
        DailyRecordEntity entity = requireOwned(userId, id);
        entity.setDeleted(true);
        dailyRecordRepository.save(entity);
        log.info(
                "[日记录] delete done entityType=DailyRecordEntity id={}, userId={}, deleted=true, elapsedMs={}",
                entity.getId(),
                entity.getUserId(),
                System.currentTimeMillis() - startMs);
    }

    /**
     * 按 id+userId 一次加载未删除记录；跨用户与缺失一律 404。
     */
    private DailyRecordEntity requireOwned(Long userId, Long id) {
        return dailyRecordRepository
                .findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> {
                    log.error("[日记录] requireOwned failed code=404 userId={}, id={}", userId, id);
                    return new NotFoundException("记录不存在");
                });
    }

    /**
     * 校验并修剪内容。
     */
    private String normalizeContent(Long userId, String raw) {
        String content = raw == null ? "" : raw.trim();
        if (content.isEmpty()) {
            log.error("[日记录] validate failed code=400 msg=请填写内容 userId={}", userId);
            throw new BusinessException("请填写内容");
        }
        if (content.length() > MAX_CONTENT) {
            log.error("[日记录] validate failed code=400 msg=内容最多500字 userId={}", userId);
            throw new BusinessException("内容最多500字");
        }
        return content;
    }

    /**
     * 解析互斥查询参数为上海时区区间：恰好一种模式；from 不得晚于 to；闭区间跨度不得超过 366 天。
     *
     * @param date      单日 YYYY-MM-DD，可空
     * @param yearMonth 整月 YYYY-MM，可空
     * @param from      区间起（含），可空
     * @param to        区间止（含），可空
     * @return 解析后的查询区间
     */
    public RecordQueryPeriod resolvePeriod(LocalDate date, YearMonth yearMonth, LocalDate from, LocalDate to) {
        // 关键入口：筛选键，不含 token
        log.info(
                "[日记录] resolvePeriod start date={}, yearMonth={}, from={}, to={}",
                date,
                yearMonth,
                from,
                to);
        boolean hasDate = date != null;
        boolean hasMonth = yearMonth != null;
        boolean hasFrom = from != null;
        boolean hasTo = to != null;
        int modes = (hasDate ? 1 : 0) + (hasMonth ? 1 : 0) + ((hasFrom || hasTo) ? 1 : 0);
        if (modes != 1 || hasFrom != hasTo) {
            log.error("[日记录] resolvePeriod failed code=400 msg=只能使用一种筛选条件");
            throw new BusinessException("只能使用一种筛选条件");
        }
        RecordQueryPeriod period;
        if (hasMonth) {
            period = RecordQueryPeriod.month(yearMonth);
        } else if (hasFrom) {
            if (from.isAfter(to)) {
                log.error("[日记录] resolvePeriod failed code=400 msg=开始日期不能晚于结束日期 from={}, to={}", from, to);
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            if (ChronoUnit.DAYS.between(from, to) > 365) {
                log.error("[日记录] resolvePeriod failed code=400 msg=查询区间不能超过366天 from={}, to={}", from, to);
                throw new BusinessException("查询区间不能超过366天");
            }
            period = RecordQueryPeriod.range(from, to);
        } else {
            period = RecordQueryPeriod.day(date);
        }
        log.info(
                "[日记录] resolvePeriod done entityType=RecordQueryPeriod date={}, yearMonth={}, from={}, to={}",
                period.getDate(),
                period.getYearMonth(),
                period.getFrom(),
                period.getTo());
        return period;
    }

    /**
     * 按已解析区间一次查询当前用户记录，时间升序。
     *
     * @param userId JWT 用户主键
     * @param period 上海时区闭开区间
     * @return 区间内列表
     */
    @Transactional(readOnly = true)
    public List<DailyRecordResponse> listByPeriod(Long userId, RecordQueryPeriod period) {
        log.info(
                "[日记录] listByPeriod start userId={}, date={}, yearMonth={}, from={}, to={}",
                userId,
                period.getDate(),
                period.getYearMonth(),
                period.getFrom(),
                period.getTo());
        // 一次按用户+区间查询，禁止按日循环查库
        List<DailyRecordEntity> rows =
                dailyRecordRepository
                        .findByUserIdAndDeletedFalseAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAscIdAsc(
                                userId, period.startInclusive(), period.endExclusive());
        List<Long> sampleIds = rows.stream().limit(20).map(DailyRecordEntity::getId).toList();
        log.info(
                "[日记录] listByPeriod loaded entityType=DailyRecordEntity size={}, sampleIds={}",
                rows.size(),
                sampleIds);
        return rows.stream().map(DailyRecordResponse::from).toList();
    }

    /**
     * 将列表结果组装为接口 data（单日带 date；月/区间带 from/to）。
     *
     * @param period 已解析区间
     * @param list   查询结果
     * @return 列表接口 data
     */
    public Map<String, Object> toListData(RecordQueryPeriod period, List<DailyRecordResponse> list) {
        log.info(
                "[日记录] toListData start date={}, yearMonth={}, from={}, to={}, size={}",
                period.getDate(),
                period.getYearMonth(),
                period.getFrom(),
                period.getTo(),
                list.size());
        Map<String, Object> data = new LinkedHashMap<>();
        if (period.getDate() != null) {
            data.put("date", period.getDate().toString());
        }
        if (period.getYearMonth() != null) {
            data.put("yearMonth", period.getYearMonth().toString());
        }
        if (period.getDate() == null) {
            data.put("from", period.getFrom().toString());
            data.put("to", period.getTo().toString());
        }
        data.put("list", list);
        log.info("[日记录] toListData done size={}", list.size());
        return data;
    }

    /**
     * 按上海时区自然日查询当前用户记录，时间升序。
     *
     * @param userId JWT 用户主键
     * @param date   选中日
     * @return 当日列表
     */
    @Transactional(readOnly = true)
    public List<DailyRecordResponse> listByDate(Long userId, LocalDate date) {
        log.info("[日记录] listByDate start userId={}, date={}", userId, date);
        // 单日走统一区间解析，避免两套时区算法
        RecordQueryPeriod period = resolvePeriod(date, null, null, null);
        List<DailyRecordResponse> list = listByPeriod(userId, period);
        log.info("[日记录] listByDate done userId={}, date={}, size={}", userId, date, list.size());
        return list;
    }

    /**
     * 按已解析区间导出 CSV，含 UTF-8 BOM；仅当前用户数据。
     *
     * @param userId JWT 用户主键
     * @param period 已解析筛选区间
     * @return CSV 字节
     */
    @Transactional(readOnly = true)
    public byte[] exportCsv(Long userId, RecordQueryPeriod period) {
        long startMs = System.currentTimeMillis();
        log.info(
                "[日记录] exportCsv start userId={}, date={}, yearMonth={}, from={}, to={}",
                userId,
                period.getDate(),
                period.getYearMonth(),
                period.getFrom(),
                period.getTo());
        // 复用区间列表查询，保证导出与列表同一批数据
        List<DailyRecordResponse> list = listByPeriod(userId, period);
        StringBuilder body = new StringBuilder();
        body.append("记录时间,类型,内容\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(SHANGHAI);
        for (DailyRecordResponse row : list) {
            body.append(formatter.format(row.getRecordedAt()))
                    .append(',')
                    .append(row.getType() == RecordType.CONSUME ? "消耗" : "摄入")
                    .append(',')
                    .append(escapeCsv(row.getContent()))
                    .append('\n');
        }
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = body.toString().getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(content, 0, out, bom.length, content.length);
        log.info(
                "[日记录] exportCsv done userId={}, from={}, to={}, rows={}, bytes={}, elapsedMs={}",
                userId,
                period.getFrom(),
                period.getTo(),
                list.size(),
                out.length,
                System.currentTimeMillis() - startMs);
        return out;
    }

    /**
     * 导出选中日 CSV，含 UTF-8 BOM；仅当前用户数据。
     *
     * @param userId JWT 用户主键
     * @param date   选中日
     * @return CSV 字节
     */
    @Transactional(readOnly = true)
    public byte[] exportCsv(Long userId, LocalDate date) {
        log.info("[日记录] exportCsv start userId={}, date={}", userId, date);
        return exportCsv(userId, resolvePeriod(date, null, null, null));
    }

    /**
     * 转义 CSV 字段中的逗号与引号。
     */
    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
