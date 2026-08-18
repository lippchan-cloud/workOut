package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
        create(user.token(), "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("workout-2026-08-18.csv")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        org.assertj.core.api.Assertions.assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        org.assertj.core.api.Assertions.assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        org.assertj.core.api.Assertions.assertThat(bytes[2]).isEqualTo((byte) 0xBF);
        String csv = new String(bytes, StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(csv).contains("记录时间,类型,内容");
        org.assertj.core.api.Assertions.assertThat(csv).contains("消耗");
        org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("CONSUME");
    }

    @Test
    void exportEmptyDayShouldContainHeaderOnly() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_empty"), "secret12");
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8).replace("\uFEFF", "");
        org.assertj.core.api.Assertions.assertThat(csv.trim()).isEqualTo("记录时间,类型,内容");
    }

    @Test
    void exportWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv").param("date", "2026-08-18"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportYearMonthShouldContainBomChineseTypeFilenameAndOnlyThatMonth() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_ym"), "secret12");
        create(user.token(), "CONSUME", "八月跑步", "2026-08-01T07:30:00+08:00");
        create(user.token(), "INTAKE", "八月早餐", "2026-08-31T08:00:00+08:00");
        create(user.token(), "CONSUME", "九月记录", "2026-09-01T00:30:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("yearMonth", "2026-08")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("workout-2026-08.csv")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        org.assertj.core.api.Assertions.assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        org.assertj.core.api.Assertions.assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        org.assertj.core.api.Assertions.assertThat(bytes[2]).isEqualTo((byte) 0xBF);
        String csv = new String(bytes, StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(csv).contains("记录时间,类型,内容");
        org.assertj.core.api.Assertions.assertThat(csv).contains("消耗");
        org.assertj.core.api.Assertions.assertThat(csv).contains("摄入");
        org.assertj.core.api.Assertions.assertThat(csv).contains("八月跑步");
        org.assertj.core.api.Assertions.assertThat(csv).contains("八月早餐");
        org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("九月记录");
        org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("CONSUME");
    }

    @Test
    void exportEmptyYearMonthShouldContainHeaderOnly() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_ym_empty"), "secret12");
        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("yearMonth", "2026-02")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8).replace("\uFEFF", "");
        org.assertj.core.api.Assertions.assertThat(csv.trim()).isEqualTo("记录时间,类型,内容");
    }

    @Test
    void exportRangeAcrossDaysShouldUseRangeFilename() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_range"), "secret12");
        create(user.token(), "CONSUME", "区间内", "2026-08-01T07:30:00+08:00");
        create(user.token(), "INTAKE", "区间外", "2026-08-19T12:00:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("from", "2026-08-01")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("workout-2026-08-01_2026-08-18.csv")))
                .andReturn();
        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(csv).contains("区间内");
        org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("区间外");
    }

    @Test
    void exportSameDayRangeShouldUseDailyFilename() throws Exception {
        AuthUser user = register(TestUsernames.unique("csv_same"), "secret12");
        create(user.token(), "CONSUME", "同日", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("from", "2026-08-18")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("workout-2026-08-18.csv")));
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
        create(a.token(), "INTAKE", "A的午餐", "2026-08-18T12:00:00+08:00");

        MvcResult result = mockMvc.perform(get("/api/v1/dailyRecords/exportCsv")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + b.token()))
                .andExpect(status().isOk())
                .andReturn();
        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(csv).doesNotContain("A的午餐");
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
