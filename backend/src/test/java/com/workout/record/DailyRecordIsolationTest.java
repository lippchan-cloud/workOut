package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class DailyRecordIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userBMustNotSeeUserARecordsOnSameDate() throws Exception {
        AuthUser userA = register("iso_a", "secret12");
        AuthUser userB = register("iso_b", "secret12");
        createConsume(userA.token(), "A only", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + userB.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    private void createConsume(String token, String content, String recordedAt) throws Exception {
        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("type", "CONSUME", "content", content, "recordedAt", recordedAt)))))
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
        return new AuthUser(data.path("token").asText(), data.path("userId").asLong());
    }

    private record AuthUser(String token, long userId) {}
}
