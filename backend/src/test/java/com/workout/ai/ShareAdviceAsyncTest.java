package com.workout.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.config.AdminProperties;
import com.workout.modules.ai.application.StubDeepSeekClient;
import com.workout.modules.ai.infrastructure.ApiKeyPoolRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.support.TestApiKeys;
import com.workout.support.TestUsernames;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 七期：无 key / stub 异步建议 / userId 边界（窄测，Stub 禁外网）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareAdviceAsyncTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminProperties adminProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserApiKeyRepository userApiKeyRepository;

    @Autowired
    private ApiKeyPoolRepository apiKeyPoolRepository;

    @Autowired
    private StubDeepSeekClient stubDeepSeekClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void resetStub() {
        stubDeepSeekClient.reset();
        stubDeepSeekClient.setFixedReply("多喝水，仅供参考");
    }

    @AfterEach
    void resetAdmin() {
        adminProperties.setUsernames("");
    }

    @Test
    void shareWithoutApiKeyShouldBeNoneKeyAndNotCallModel() throws Exception {
        String username = TestUsernames.unique("nokey");
        String token = register(username, "secret12");
        Long userId = userRepository.findByUsername(username).map(UserEntity::getId).orElseThrow();
        // 注册可能已从密钥库默认绑定；本用例要测无 Key
        transactionTemplate.executeWithoutResult(status -> userApiKeyRepository.deleteByUserId(userId));
        putProfile(token, "无钥", 170.0, 65.0);
        createRecord(token, "CONSUME", "快走", "2026-08-18T07:30:00+08:00");

        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/reports/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adviceStatus").value("NONE_KEY"))
                .andExpect(jsonPath("$.data.advice").value("未配置 API Key"));
        assertThat(stubDeepSeekClient.getInvokeCount()).isZero();
    }

    @Test
    void shareWithKeyShouldAsyncFillAdviceViaStub() throws Exception {
        String username = TestUsernames.unique("withkey");
        String token = register(username, "secret12");
        Long userId = userRepository.findByUsername(username).map(UserEntity::getId).orElseThrow();
        bindFakeKey(userId, "sk-test-fake-key-aaaa");
        putProfile(token, "有钥", 170.0, 65.0);
        createRecord(token, "CONSUME", "游泳", "2026-08-18T07:30:00+08:00");

        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isString())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/reports/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adviceStatus").value("READY"))
                .andExpect(jsonPath("$.data.advice").value("多喝水，仅供参考"));
        assertThat(stubDeepSeekClient.getInvokeCount()).isEqualTo(1);
        assertThat(stubDeepSeekClient.getLastUserPrompt()).contains("userId=" + userId);
    }

    @Test
    void stubFailureShouldNotBreakShareCreate() throws Exception {
        String username = TestUsernames.unique("failai");
        String token = register(username, "secret12");
        Long userId = userRepository.findByUsername(username).map(UserEntity::getId).orElseThrow();
        bindFakeKey(userId, "sk-test-fake-key-bbbb");
        stubDeepSeekClient.failNext();
        putProfile(token, "失败", 170.0, 65.0);
        createRecord(token, "CONSUME", "骑行", "2026-08-18T07:30:00+08:00");

        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();
        mockMvc.perform(get("/api/v1/reports/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adviceStatus").value("FAILED"));
    }

    private void bindFakeKey(Long userId, String fakeKey) {
        TestApiKeys.bind(apiKeyPoolRepository, userApiKeyRepository, userId, fakeKey);
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
