package com.workout.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.modules.ai.application.AiRateLimitService;
import com.workout.modules.ai.application.StubDeepSeekClient;
import com.workout.modules.ai.domain.AiCallPurpose;
import com.workout.modules.ai.infrastructure.AiCallLogEntity;
import com.workout.modules.ai.infrastructure.AiCallLogRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.support.TestUsernames;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 七期限流：MySQL COUNT 权威；超限不调 Stub。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiAdviceRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserApiKeyRepository userApiKeyRepository;

    @Autowired
    private AiCallLogRepository aiCallLogRepository;

    @Autowired
    private StubDeepSeekClient stubDeepSeekClient;

    @BeforeEach
    void resetStub() {
        stubDeepSeekClient.reset();
    }

    @Test
    void hourlyLimitShouldBlockDeepSeekViaMysqlCount() throws Exception {
        String username = TestUsernames.unique("rateh");
        String token = register(username, "secret12");
        Long userId = userRepository.findByUsername(username).map(UserEntity::getId).orElseThrow();
        UserApiKeyEntity key = bindFakeKey(userId, "sk-test-fake-key-rate");
        // 先插入本小时 10 条日志，权威在 MySQL
        for (int i = 0; i < AiRateLimitService.HOUR_LIMIT; i++) {
            AiCallLogEntity logRow = new AiCallLogEntity();
            logRow.setUserId(userId);
            logRow.setApiKeyId(key.getId());
            logRow.setPurpose(AiCallPurpose.SHARE_ADVICE.name());
            logRow.setStatus("SUCCESS");
            logRow.setCreatedAt(Instant.now());
            aiCallLogRepository.save(logRow);
        }
        putProfile(token, "限流", 170.0, 65.0);
        createRecord(token, "CONSUME", "跳绳", "2026-08-18T07:30:00+08:00");

        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();
        mockMvc.perform(get("/api/v1/reports/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adviceStatus").value("FAILED"));
        assertThat(stubDeepSeekClient.getInvokeCount()).isZero();
    }

    private UserApiKeyEntity bindFakeKey(Long userId, String fakeKey) {
        UserApiKeyEntity row = new UserApiKeyEntity();
        row.setUserId(userId);
        row.setApiKey(fakeKey);
        row.setKeyMask("****" + fakeKey.substring(fakeKey.length() - 4));
        row.setUpdatedAt(Instant.now());
        return userApiKeyRepository.save(row);
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
                        .content(objectMapper.writeValueAsString(
                                Map.of("request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }
}
