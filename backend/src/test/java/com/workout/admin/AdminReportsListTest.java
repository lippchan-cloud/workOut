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
import com.workout.support.TestUsernames;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminReportsListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminProperties adminProperties;

    @AfterEach
    void resetAdminBootstrap() {
        adminProperties.setUsernames("");
    }

    @Test
    void listReportsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void regularUserJwtCannotListReports() throws Exception {
        JsonNode user = register(TestUsernames.unique("cms_rp_user"), "secret12");
        mockMvc.perform(get("/api/v1/admin/reports")
                        .header("Authorization", "Bearer " + user.path("token").asText()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminJwtCanListExistingShares() throws Exception {
        String adminName = TestUsernames.unique("cms_rp_adm");
        adminProperties.setUsernames(adminName);
        JsonNode admin = register(adminName, "secret12");

        String aliceName = TestUsernames.unique("cms_rp_alice");
        JsonNode alice = register(aliceName, "secret12");
        putProfile(alice.path("token").asText(), "阿丽", 165.0, 52.0);
        createRecord(alice.path("token").asText(), "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");
        String shareId = createShare(alice.path("token").asText(), "2026-08-18");

        MvcResult result = mockMvc.perform(get("/api/v1/admin/reports")
                        .header("Authorization", "Bearer " + admin.path("token").asText()))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode list = objectMapper.readTree(body).path("data").path("list");
        JsonNode item = findByUsername(list, aliceName);
        assertThat(item.path("id").asText()).isEqualTo(shareId);
        assertThat(item.path("userId").asLong()).isEqualTo(alice.path("userId").asLong());
        assertThat(item.path("username").asText()).isEqualTo(aliceName);
        assertThat(item.path("from").asText()).isEqualTo("2026-08-18");
        assertThat(item.path("to").asText()).isEqualTo("2026-08-18");
        assertThat(item.path("createdAt").asText()).isNotBlank();
        assertThat(body).doesNotContain("passwordHash");
    }

    private JsonNode findByUsername(JsonNode list, String username) {
        assertThat(list.isArray()).isTrue();
        for (JsonNode node : list) {
            if (username.equals(node.path("username").asText())) {
                return node;
            }
        }
        throw new AssertionError("share not listed for username: " + username);
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

    private String createShare(String token, String date) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
    }

    private JsonNode register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
