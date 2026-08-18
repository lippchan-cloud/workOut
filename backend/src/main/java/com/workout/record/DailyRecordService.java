package com.workout.record;

import com.workout.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日记录应用服务（应用层）。
 * 负责创建与按日查询；一律使用 JWT 中的 userId，禁止信任客户端身份字段。
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
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            log.error("[日记录] create failed code=400 msg=请填写内容 userId={}", userId);
            throw new BusinessException("请填写内容");
        }
        if (content.length() > MAX_CONTENT) {
            log.error("[日记录] create failed code=400 msg=内容最多500字 userId={}", userId);
            throw new BusinessException("内容最多500字");
        }

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
     * 按上海时区自然日查询当前用户记录，时间升序。
     *
     * @param userId JWT 用户主键
     * @param date   选中日
     * @return 当日列表
     */
    @Transactional(readOnly = true)
    public List<DailyRecordResponse> listByDate(Long userId, LocalDate date) {
        log.info("[日记录] listByDate start userId={}, date={}", userId, date);
        Instant start = date.atStartOfDay(SHANGHAI).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(SHANGHAI).toInstant();
        // 一次按用户+区间查询，禁止循环查库
        List<DailyRecordEntity> rows =
                dailyRecordRepository
                        .findByUserIdAndDeletedFalseAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAscIdAsc(
                                userId, start, end);
        log.info("[日记录] listByDate done userId={}, date={}, size={}", userId, date, rows.size());
        return rows.stream().map(DailyRecordResponse::from).toList();
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
        List<DailyRecordResponse> list = listByDate(userId, date);
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
        log.info("[日记录] exportCsv done userId={}, date={}, rows={}, bytes={}", userId, date, list.size(), out.length);
        return out;
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
