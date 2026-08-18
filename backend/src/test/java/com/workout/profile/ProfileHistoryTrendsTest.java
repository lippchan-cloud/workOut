package com.workout.profile;

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
class ProfileHistoryTrendsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void firstSaveShouldCreateOneHistorySnapshot() throws Exception {
        String token = register(TestUsernames.unique("hist_first"), "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 175.0, 70.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profile/trends").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bodyHistory.length()").value(1))
                .andExpect(jsonPath("$.data.bodyHistory[0].heightCm").value(175.0))
                .andExpect(jsonPath("$.data.bodyHistory[0].weightKg").value(70.0))
                .andExpect(jsonPath("$.data.bodyHistory[0].nickname").value("小明"));
    }

    @Test
    void secondSaveWithHeightChangeShouldAppendSnapshotInChangedAtOrder() throws Exception {
        String token = register(TestUsernames.unique("hist_chg"), "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 175.0, 70.0)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 176.0, 70.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profile/trends").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyHistory.length()").value(2))
                .andExpect(jsonPath("$.data.bodyHistory[0].heightCm").value(175.0))
                .andExpect(jsonPath("$.data.bodyHistory[1].heightCm").value(176.0));
    }

    @Test
    void unchangedSaveShouldNotAppendHistory() throws Exception {
        String token = register(TestUsernames.unique("hist_same"), "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 175.0, 70.0)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 175.0, 70.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profile/trends").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyHistory.length()").value(1));
    }

    @Test
    void trendsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/profile/trends")).andExpect(status().isUnauthorized());
    }

    @Test
    void trendsShouldIncludeOwnRecordCountsAndHideOtherUsers() throws Exception {
        String tokenA = register(TestUsernames.unique("hist_iso_a"), "secret12");
        String tokenB = register(TestUsernames.unique("hist_iso_b"), "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("只属于A", 165.0, 52.0)))
                .andExpect(status().isOk());
        createRecord(tokenA, "CONSUME", "A跑步", "2026-08-18T07:30:00+08:00");
        createRecord(tokenA, "INTAKE", "A早餐", "2026-08-18T08:00:00+08:00");

        mockMvc.perform(get("/api/v1/profile/trends").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyHistory.length()").value(1))
                .andExpect(jsonPath("$.data.recordCounts[?(@.date=='2026-08-18')].count").value(2));

        mockMvc.perform(get("/api/v1/profile/trends").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bodyHistory.length()").value(0))
                .andExpect(jsonPath("$.data.recordCounts.length()").value(0));
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

    private String profileBody(String nickname, double heightCm, double weightKg) throws Exception {
        Map<String, Object> inner = new HashMap<>();
        inner.put("nickname", nickname);
        inner.put("heightCm", heightCm);
        inner.put("weightKg", weightKg);
        return objectMapper.writeValueAsString(Map.of("request", inner));
    }

    private String register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }
}
