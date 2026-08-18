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
class AdminUserDetailTest {

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
    void detailWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void regularUserJwtCannotLoadDetail() throws Exception {
        JsonNode user = register(TestUsernames.unique("cms_ud_user"), "secret12");
        mockMvc.perform(get("/api/v1/admin/accounts/" + user.path("userId").asLong())
                        .header("Authorization", "Bearer " + user.path("token").asText()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminJwtCanLoadUserDetailWithRecordsAndShares() throws Exception {
        String adminName = TestUsernames.unique("cms_ud_adm");
        adminProperties.setUsernames(adminName);
        JsonNode admin = register(adminName, "secret12");

        String aliceName = TestUsernames.unique("cms_ud_alice");
        JsonNode alice = register(aliceName, "secret12");
        long aliceId = alice.path("userId").asLong();
        String aliceToken = alice.path("token").asText();
        putProfile(aliceToken, "阿丽", 165.0, 52.0);
        createRecord(aliceToken, "CONSUME", "跑步", "2026-08-18T07:30:00+08:00");
        String shareId = createShare(aliceToken, "2026-08-18");

        MvcResult result = mockMvc.perform(get("/api/v1/admin/accounts/" + aliceId)
                        .header("Authorization", "Bearer " + admin.path("token").asText()))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode data = objectMapper.readTree(body).path("data");
        assertThat(data.path("userId").asLong()).isEqualTo(aliceId);
        assertThat(data.path("username").asText()).isEqualTo(aliceName);
        assertThat(data.path("role").asText()).isEqualTo("USER");
        assertThat(data.path("createdAt").asText()).isNotBlank();
        assertThat(data.path("nickname").asText()).isEqualTo("阿丽");
        assertThat(data.path("heightCm").asDouble()).isEqualTo(165.0);
        assertThat(data.path("weightKg").asDouble()).isEqualTo(52.0);
        assertThat(data.path("recordCount").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(data.path("recentRecords").isArray()).isTrue();
        assertThat(data.path("recentRecords")).isNotEmpty();
        assertThat(data.path("recentRecords").get(0).path("content").asText()).isEqualTo("跑步");
        JsonNode share = data.path("shares").get(0);
        assertThat(share.path("id").asText()).isEqualTo(shareId);
        assertThat(share.path("from").asText()).isEqualTo("2026-08-18");
        assertThat(share.path("to").asText()).isEqualTo("2026-08-18");
        assertThat(share.path("createdAt").asText()).isNotBlank();
        assertThat(body).doesNotContain("passwordHash");
        assertThat(body).doesNotContain("\"password\"");
    }

    @Test
    void unknownUserShouldReturn404() throws Exception {
        String adminName = TestUsernames.unique("cms_ud_404");
        adminProperties.setUsernames(adminName);
        JsonNode admin = register(adminName, "secret12");
        mockMvc.perform(get("/api/v1/admin/accounts/999999999")
                        .header("Authorization", "Bearer " + admin.path("token").asText()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
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
