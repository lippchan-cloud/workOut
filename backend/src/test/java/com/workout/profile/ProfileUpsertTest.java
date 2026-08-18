package com.workout.profile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ProfileUpsertTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void putThenGetShouldEchoSavedProfile() throws Exception {
        String token = register("prof_a", "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("小明", 175.0, 70.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("小明"))
                .andExpect(jsonPath("$.data.heightCm").value(175.0))
                .andExpect(jsonPath("$.data.weightKg").value(70.0));
    }

    @Test
    void heightOutOfRangeShouldReturn400() throws Exception {
        String token = register("prof_h", "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("高", 300.0, 70.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("50")))
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("250")));
    }

    @Test
    void userBMustNotReadUserAProfile() throws Exception {
        String tokenA = register("prof_iso_a", "secret12");
        String tokenB = register("prof_iso_b", "secret12");
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("只属于A", 175.0, 70.0)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profile").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(org.hamcrest.Matchers.not("只属于A")));
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
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }
}
