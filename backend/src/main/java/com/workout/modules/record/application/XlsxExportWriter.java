package com.workout.modules.record.application;

import com.workout.modules.profile.infrastructure.ProfileHistoryEntity;
import com.workout.modules.record.api.DailyRecordResponse;
import com.workout.modules.record.domain.RecordType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将事项列表与成长曲线写成 xlsx 字节（应用层辅助）。
 * 不查库；由调用方一次加载后传入。
 */
final class XlsxExportWriter {

    private static final Logger log = LoggerFactory.getLogger(XlsxExportWriter.class);
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(SHANGHAI);

    private XlsxExportWriter() {}

    /**
     * 生成双工作表工作簿：事项列表（无身体列）+ 成长曲线。
     */
    static byte[] write(List<DailyRecordResponse> records, List<ProfileHistoryEntity> history) {
        log.info("[日记录] XlsxExportWriter.write start records={}, history={}", records.size(), history.size());
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeRecordsSheet(workbook, records);
            writeCurveSheet(workbook, history);
            workbook.write(out);
            byte[] bytes = out.toByteArray();
            log.info("[日记录] XlsxExportWriter.write done bytes={}", bytes.length);
            return bytes;
        } catch (IOException ex) {
            throw new UncheckedIOException("生成导出表格失败", ex);
        }
    }

    /**
     * 事项列表：仅时间、类型、内容；身体数据不进本表。
     */
    private static void writeRecordsSheet(XSSFWorkbook workbook, List<DailyRecordResponse> records) {
        Sheet sheet = workbook.createSheet("事项列表");
        Row header = sheet.createRow(0);
        writeCells(header, "记录时间", "类型", "内容");
        int rowIndex = 1;
        for (DailyRecordResponse row : records) {
            Row excelRow = sheet.createRow(rowIndex++);
            writeCells(
                    excelRow,
                    DATE_TIME.format(row.getRecordedAt()),
                    row.getType() == RecordType.CONSUME ? "消耗" : "摄入",
                    row.getContent());
        }
    }

    /**
     * 成长曲线：历史点时间、身高厘米、体重千克。
     */
    private static void writeCurveSheet(XSSFWorkbook workbook, List<ProfileHistoryEntity> history) {
        Sheet sheet = workbook.createSheet("成长曲线");
        Row header = sheet.createRow(0);
        writeCells(header, "时间", "身高cm", "体重kg");
        int rowIndex = 1;
        for (ProfileHistoryEntity point : history) {
            Row excelRow = sheet.createRow(rowIndex++);
            writeCells(
                    excelRow,
                    DATE_TIME.format(point.getChangedAt()),
                    formatDecimal(point.getHeightCm()),
                    formatDecimal(point.getWeightKg()));
        }
    }

    /**
     * 按列写入文本单元格。
     */
    private static void writeCells(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] == null ? "" : values[i]);
        }
    }

    /**
     * 身高体重导出为纯数字文本；空则空串。
     */
    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }
}
