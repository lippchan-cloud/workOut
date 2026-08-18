package com.workout.record;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyRecordCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createConsumeShouldPersistWithJwtUserId() throws Exception {
        AuthUser alice = register(TestUsernames.unique("alice_rec"), "secret12");

        MvcResult result = mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + alice.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of(
                                        "type", "CONSUME",
                                        "content", "跑步 30 分钟",
                                        "recordedAt", "2026-08-18T07:30:00+08:00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value("CONSUME"))
                .andExpect(jsonPath("$.data.content").value("跑步 30 分钟"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        long recordId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
        Long userId = jdbcTemplate.queryForObject(
                "select user_id from daily_record where id = ?", Long.class, recordId);
        assertThat(userId).isEqualTo(alice.userId());
    }

    @Test
    void emptyContentShouldReturn400() throws Exception {
        AuthUser alice = register(TestUsernames.unique("alice_empty"), "secret12");

        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + alice.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of(
                                        "type", "CONSUME",
                                        "content", "   ",
                                        "recordedAt", "2026-08-18T07:30:00+08:00")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请填写内容"));
    }

    @Test
    void contentLongerThan500ShouldReturn400() throws Exception {
        AuthUser alice = register(TestUsernames.unique("alice_long"), "secret12");
        String content = "a".repeat(501);

        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + alice.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of(
                                        "type", "INTAKE",
                                        "content", content,
                                        "recordedAt", "2026-08-18T08:00:00+08:00")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("500")));
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
