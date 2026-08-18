package com.workout.share;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.util.HashMap;
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
class ShareReportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createShareShouldReturnRandomTokenAndConfiguredUrl() throws Exception {
        String token = register(TestUsernames.unique("share_ok"), "secret12");
        putProfile(token, "小明", 175.0, 70.0);
        createRecord(token, "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");

        MvcResult result = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isString())
                .andExpect(jsonPath("$.data.url").isString())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        String id = data.path("id").asText();
        org.assertj.core.api.Assertions.assertThat(id).hasSizeGreaterThan(16);
        org.assertj.core.api.Assertions.assertThat(id).doesNotMatch("^\\d{1,6}$");
        org.assertj.core.api.Assertions.assertThat(data.path("url").asText())
                .isEqualTo("http://localhost:8080/report/" + id);
    }

    @Test
    void createShareWithoutHeightShouldReturn400() throws Exception {
        String token = register(TestUsernames.unique("share_noh"), "secret12");
        mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请先填写身高和体重"));
    }

    @Test
    void createShareWithoutJwtShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/shareReports").param("date", "2026-08-18"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousGetShouldReturnSnapshot() throws Exception {
        String token = register(TestUsernames.unique("share_get"), "secret12");
        putProfile(token, "小明", 175.0, 70.0);
        createRecord(token, "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");
        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/reports/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.displayName").value("小明"))
                .andExpect(jsonPath("$.data.records[0].content").value("跑步"))
                .andExpect(jsonPath("$.data.from").value("2026-08-18"))
                .andExpect(jsonPath("$.data.to").value("2026-08-18"))
                .andExpect(jsonPath("$.data.bodyHistory").isArray())
                .andExpect(jsonPath("$.data.advice").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void unknownReportShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/reports/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("报告不存在"));
    }

    private void putProfile(String token, String nickname, double heightCm, double weightKg) throws Exception {
        Map<String, Object> inner = new HashMap<>();
        inner.put("nickname", nickname);
        inner.put("heightCm", heightCm);
        inner.put("weightKg", weightKg);
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("request", inner))))
                .andExpect(status().isOk());
    }

    private void createRecord(String token, String type, String content, String recordedAt) throws Exception {
        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("type", type, "content", content, "recordedAt", recordedAt)))))
                .andExpect(status().isOk());
    }

    private String register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }
}
