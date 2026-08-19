package com.workout.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
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
class MyShareReportsListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listWithoutJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/shareReports")).andExpect(status().isUnauthorized());
    }

    @Test
    void ownerSeesOwnShareAndOtherUserDoesNot() throws Exception {
        String aliceToken = register(TestUsernames.unique("myrep_a"), "secret12");
        putProfile(aliceToken, "小明", 175.0, 70.0);
        MvcResult created = mockMvc.perform(post("/api/v1/shareReports")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andReturn();
        String shareId = objectMapper.readTree(created.getResponse().getContentAsString()).path("data").path("id").asText();

        MvcResult aliceList = mockMvc.perform(get("/api/v1/shareReports").header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode aliceItems = objectMapper.readTree(aliceList.getResponse().getContentAsString()).path("data").path("list");
        assertThat(aliceItems.toString()).contains(shareId);

        String bobToken = register(TestUsernames.unique("myrep_b"), "secret12");
        MvcResult bobList = mockMvc.perform(get("/api/v1/shareReports").header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode bobItems = objectMapper.readTree(bobList.getResponse().getContentAsString()).path("data").path("list");
        assertThat(bobItems.toString()).doesNotContain(shareId);
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
