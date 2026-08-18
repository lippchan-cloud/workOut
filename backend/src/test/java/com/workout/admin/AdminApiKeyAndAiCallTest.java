package com.workout.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.config.AdminProperties;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.support.TestUsernames;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * CMS API Key / AI 调用鉴权与掩码（窄测）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminApiKeyAndAiCallTest {

    private static final String FAKE_KEY = "sk-test-fake-cms-key-zzzz";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminProperties adminProperties;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void resetAdmin() {
        adminProperties.setUsernames("");
    }

    @Test
    void anonymousApiKeysShould401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/apiKeys")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/aiCalls")).andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotManageApiKeysOrListCalls() throws Exception {
        String userToken = register(TestUsernames.unique("cms_u"), "secret12").path("token").asText();
        mockMvc.perform(get("/api/v1/admin/apiKeys").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/aiCalls").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUpsertAndBatchAndListMasked() throws Exception {
        String adminName = TestUsernames.unique("p7adm");
        adminProperties.setUsernames(adminName);
        String adminToken = register(adminName, "secret12").path("token").asText();

        String u1 = TestUsernames.unique("p7a");
        String u2 = TestUsernames.unique("p7b");
        register(u1, "secret12");
        register(u2, "secret12");
        Long id1 = userRepository.findByUsername(u1).map(UserEntity::getId).orElseThrow();
        Long id2 = userRepository.findByUsername(u2).map(UserEntity::getId).orElseThrow();

        MvcResult one = mockMvc.perform(put("/api/v1/admin/apiKeys/" + id1)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("request", Map.of("apiKey", FAKE_KEY)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyMask").value("****zzzz"))
                .andReturn();
        String bodyOne = one.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(bodyOne).doesNotContain(FAKE_KEY);

        mockMvc.perform(put("/api/v1/admin/apiKeys/batch")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("userIds", List.of(id1, id2), "apiKey", FAKE_KEY)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(2));

        MvcResult list = mockMvc.perform(get("/api/v1/admin/apiKeys")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String listBody = list.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(listBody).contains("****zzzz");
        assertThat(listBody).doesNotContain(FAKE_KEY);

        mockMvc.perform(get("/api/v1/admin/aiCalls").param("userId", String.valueOf(id1))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    private JsonNode register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
