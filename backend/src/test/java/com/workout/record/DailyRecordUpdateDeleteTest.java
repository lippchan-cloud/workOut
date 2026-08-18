package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class DailyRecordUpdateDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ownerCanUpdateConsumeContent() throws Exception {
        AuthUser alice = register(TestUsernames.unique("upd_a"), "secret12");
        long id = createConsume(alice.token(), "跑步 30 分钟", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(put("/api/v1/dailyRecords/" + id)
                        .header("Authorization", "Bearer " + alice.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody("CONSUME", "跑步 45 分钟", "2026-08-18T07:45:00+08:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("跑步 45 分钟"));

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].content").value("跑步 45 分钟"));
    }

    @Test
    void emptyContentOnUpdateShouldReturn400() throws Exception {
        AuthUser alice = register(TestUsernames.unique("upd_empty"), "secret12");
        long id = createConsume(alice.token(), "跑步 30 分钟", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(put("/api/v1/dailyRecords/" + id)
                        .header("Authorization", "Bearer " + alice.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody("CONSUME", "   ", "2026-08-18T07:30:00+08:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请填写内容"));
    }

    @Test
    void ownerCanDeleteRecordThenListOmitsIt() throws Exception {
        AuthUser alice = register(TestUsernames.unique("del_a"), "secret12");
        long id = createConsume(alice.token(), "要删的", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(delete("/api/v1/dailyRecords/" + id)
                        .header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    void otherUserUpdateOrDeleteShouldReturn404() throws Exception {
        AuthUser alice = register(TestUsernames.unique("iso_upd_a"), "secret12");
        AuthUser bob = register(TestUsernames.unique("iso_upd_b"), "secret12");
        long id = createConsume(alice.token(), "A only", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(put("/api/v1/dailyRecords/" + id)
                        .header("Authorization", "Bearer " + bob.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody("CONSUME", "篡改", "2026-08-18T07:30:00+08:00")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/dailyRecords/" + id)
                        .header("Authorization", "Bearer " + bob.token()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].content").value("A only"));
    }

    private String recordBody(String type, String content, String recordedAt) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "request", Map.of("type", type, "content", content, "recordedAt", recordedAt)));
    }

    private long createConsume(String token, String content, String recordedAt) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody("CONSUME", content, recordedAt)))
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
