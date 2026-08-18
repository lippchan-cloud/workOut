package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyRecordGetByIdTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ownerCanGetOwnConsumeRecord() throws Exception {
        AuthUser alice = register(TestUsernames.unique("get_a"), "secret12");
        long id = createConsume(alice.token(), "跑步", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords/" + id).header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.type").value("CONSUME"))
                .andExpect(jsonPath("$.data.content").value("跑步"));
    }

    @Test
    void otherUserGetShouldReturn404() throws Exception {
        AuthUser alice = register(TestUsernames.unique("get_iso_a"), "secret12");
        AuthUser bob = register(TestUsernames.unique("get_iso_b"), "secret12");
        long id = createConsume(alice.token(), "A only", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords/" + id).header("Authorization", "Bearer " + bob.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("记录不存在"));
    }

    @Test
    void missingIdShouldReturn404() throws Exception {
        AuthUser alice = register(TestUsernames.unique("get_miss"), "secret12");

        mockMvc.perform(get("/api/v1/dailyRecords/999999999").header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("记录不存在"));
    }

    private long createConsume(String token, String content, String recordedAt) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("type", "CONSUME", "content", content, "recordedAt", recordedAt)))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
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
