package com.workout.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.nio.charset.StandardCharsets;
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
class AdminAccountsListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listAccountsWithoutTokenShouldIncludeRegisteredUserAndProfile() throws Exception {
        String username = TestUsernames.unique("cms_alice");
        String token = register(username, "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("阿丽", 165.0, 52.0)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/accounts"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("code").asInt()).isEqualTo(200);
        JsonNode item = findAccount(root.path("data").path("list"), username);
        assertThat(item.path("userId").isNumber()).isTrue();
        assertThat(item.path("username").asText()).isEqualTo(username);
        assertThat(item.path("createdAt").asText()).isNotBlank();
        assertThat(item.path("nickname").asText()).isEqualTo("阿丽");
        assertThat(item.path("heightCm").asDouble()).isEqualTo(165.0);
        assertThat(item.path("weightKg").asDouble()).isEqualTo(52.0);
        assertThat(body).doesNotContain("passwordHash");
        assertThat(body).doesNotContain("\"password\"");
    }

    @Test
    void accountWithoutProfileShouldHaveNullProfileFields() throws Exception {
        String username = TestUsernames.unique("cms_bob");
        register(username, "secret12");

        MvcResult result = mockMvc.perform(get("/api/v1/admin/accounts"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode item = findAccount(
                objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data").path("list"),
                username);
        assertThat(item.path("nickname").isNull()).isTrue();
        assertThat(item.path("heightCm").isNull()).isTrue();
        assertThat(item.path("weightKg").isNull()).isTrue();
    }

    @Test
    void profileWithoutTokenShouldStillReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void dailyRecordsWithoutTokenShouldStillReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dailyRecords").param("date", "2026-08-18"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private JsonNode findAccount(JsonNode list, String username) {
        assertThat(list.isArray()).isTrue();
        for (JsonNode node : list) {
            if (username.equals(node.path("username").asText())) {
                return node;
            }
        }
        throw new AssertionError("account not listed: " + username);
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
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }
}
