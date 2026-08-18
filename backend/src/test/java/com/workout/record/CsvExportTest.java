package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsvExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportWithDataShouldContainBomHeaderChineseTypeAndFilename() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_user"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        create(user.token(), "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("workout-2026-08-18.xlsx")))
                .andReturn();

        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items).contains("记录时间,类型,内容,昵称,身高cm,体重kg");
        org.assertj.core.api.Assertions.assertThat(items).contains("消耗");
        org.assertj.core.api.Assertions.assertThat(items).contains("跑步");
        org.assertj.core.api.Assertions.assertThat(items).doesNotContain("CONSUME");
    }

    @Test
    void exportEmptyDayShouldContainHeaderOnly() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_empty"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items.trim()).isEqualTo("记录时间,类型,内容,昵称,身高cm,体重kg");
    }

    @Test
    void exportWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv").param("date", "2026-08-18"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportYearMonthShouldContainBomChineseTypeFilenameAndOnlyThatMonth() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_ym"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        create(user.token(), "CONSUME", "八月跑步", "2026-08-01T07:30:00+08:00");
        create(user.token(), "INTAKE", "八月早餐", "2026-08-31T08:00:00+08:00");
        create(user.token(), "CONSUME", "九月记录", "2026-09-01T00:30:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("yearMonth", "2026-08")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("workout-2026-08.xlsx")))
                .andReturn();

        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items).contains("记录时间,类型,内容,昵称,身高cm,体重kg");
        org.assertj.core.api.Assertions.assertThat(items).contains("消耗");
        org.assertj.core.api.Assertions.assertThat(items).contains("摄入");
        org.assertj.core.api.Assertions.assertThat(items).contains("八月跑步");
        org.assertj.core.api.Assertions.assertThat(items).contains("八月早餐");
        org.assertj.core.api.Assertions.assertThat(items).doesNotContain("九月记录");
        org.assertj.core.api.Assertions.assertThat(items).doesNotContain("CONSUME");
    }

    @Test
    void exportEmptyYearMonthShouldContainHeaderOnly() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_ym_empty"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("yearMonth", "2026-02")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items.trim()).isEqualTo("记录时间,类型,内容,昵称,身高cm,体重kg");
    }

    @Test
    void exportRangeAcrossDaysShouldUseRangeFilename() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_range"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        create(user.token(), "CONSUME", "区间内", "2026-08-01T07:30:00+08:00");
        create(user.token(), "INTAKE", "区间外", "2026-08-19T12:00:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("from", "2026-08-01")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("workout-2026-08-01_2026-08-18.xlsx")))
                .andReturn();
        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items).contains("区间内");
        org.assertj.core.api.Assertions.assertThat(items).doesNotContain("区间外");
    }

    @Test
    void exportSameDayRangeShouldUseDailyFilename() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_same"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        create(user.token(), "CONSUME", "同日", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("from", "2026-08-18")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("workout-2026-08-18.xlsx")));
    }

    @Test
    void exportMixingDateAndYearMonthShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_mix"), "secret12");
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .param("yearMonth", "2026-08")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportYearMonthWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv").param("yearMonth", "2026-08"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportShouldNotIncludeOtherUsersRows() throws Exception {
        AuthUser a = register(TestUsernames.unique("csv_a"), "secret12");
        AuthUser b = register(TestUsernames.unique("csv_b"), "secret12");
        putProfile(a.token(), "A", 175.0, 70.0);
        putProfile(b.token(), "B", 176.0, 71.0);
        create(a.token(), "INTAKE", "A的午餐", "2026-08-18T12:00:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + b.token()))
                .andExpect(status().isOk())
                .andReturn();
        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        org.assertj.core.api.Assertions.assertThat(items).doesNotContain("A的午餐");
    }

    @Test
    void exportRowsShouldAlignHeightToHistoryAtRecordedAt() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_hist"), "secret12");
        putProfile(user.token(), "对齐", 170.0, 70.0);
        Instant earlyAt = Instant.now().plusSeconds(1);
        create(user.token(), "CONSUME", "早训", earlyAt.toString());
        Thread.sleep(1300);
        putProfile(user.token(), "对齐", 180.0, 70.0);
        Instant lateAt = Instant.now().plusSeconds(1);
        create(user.token(), "CONSUME", "晚训", lateAt.toString());

        String from = earlyAt.atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().toString();
        String to = lateAt.atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().toString();
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("from", from)
                        .param("to", to)
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        String items = sheetJoined(result.getResponse().getContentAsByteArray(), "事项列表");
        String early = java.util.Arrays.stream(items.split("\\R"))
                .filter(line -> line.contains("早训"))
                .findFirst()
                .orElse("");
        String late = java.util.Arrays.stream(items.split("\\R"))
                .filter(line -> line.contains("晚训"))
                .findFirst()
                .orElse("");
        org.assertj.core.api.Assertions.assertThat(early).contains("170");
        org.assertj.core.api.Assertions.assertThat(early).doesNotContain("180");
        org.assertj.core.api.Assertions.assertThat(late).contains("180");
    }

    @Test
    void exportEmptyDayShouldBeXlsxWithTwoSheets() throws Exception {
        AuthUser user = register(TestUsernames.unique("xlsx_empty"), "secret12");
        putProfile(user.token(), "导出", 175.0, 70.0);
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("workout-2026-08-18.xlsx")))
                .andReturn();
        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            org.apache.poi.ss.usermodel.Sheet items = workbook.getSheet("事项列表");
            org.apache.poi.ss.usermodel.Sheet curve = workbook.getSheet("成长曲线");
            org.assertj.core.api.Assertions.assertThat(items).isNotNull();
            org.assertj.core.api.Assertions.assertThat(curve).isNotNull();
            org.assertj.core.api.Assertions.assertThat(joinRow(items.getRow(0)))
                    .isEqualTo("记录时间,类型,内容,昵称,身高cm,体重kg");
            org.assertj.core.api.Assertions.assertThat(joinRow(curve.getRow(0))).isEqualTo("时间,身高cm,体重kg");
        }
    }

    private String sheetJoined(byte[] bytes, String sheetName) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet(sheetName);
            StringBuilder all = new StringBuilder();
            for (Row row : sheet) {
                all.append(joinRow(row)).append('\n');
            }
            return all.toString();
        }
    }

    private String joinRow(Row row) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (i > 0) {
                line.append(',');
            }
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            line.append(cell == null ? "" : cell.toString().trim());
        }
        return line.toString();
    }

    @Test
    void exportWithoutHeightOrWeightShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_nobody"), "secret12");
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请先填写身高和体重"));
    }

    @Test
    void exportWithWeightOnlyShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_nowh"), "secret12");
        putProfilePartial(user.token(), "半成品", null, 70.0);
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请先填写身高和体重"));
    }

    private void putProfilePartial(String token, String nickname, Double heightCm, double weightKg) throws Exception {
        java.util.HashMap<String, Object> inner = new java.util.HashMap<>();
        inner.put("nickname", nickname);
        inner.put("weightKg", weightKg);
        if (heightCm != null) {
            inner.put("heightCm", heightCm);
        }
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("request", inner))))
                .andExpect(status().isOk());
    }

    private void putProfile(String token, String nickname, double heightCm, double weightKg) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("nickname", nickname, "heightCm", heightCm, "weightKg", weightKg)))))
                .andExpect(status().isOk());
    }

    private void create(String token, String type, String content, String recordedAt) throws Exception {
        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("type", type, "content", content, "recordedAt", recordedAt)))))
                .andExpect(status().isOk());
    }

    private AuthUser register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new AuthUser(data.path("token").asText());
    }

    private record AuthUser(String token) {}
}
