package com.workout.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.CapturingEmailSender;
import com.workout.support.TestUsernames;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CapturingEmailSender.class)
class EmailBindLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CapturingEmailSender capturingEmailSender;

    @Test
    void bindThenEmailLoginThenUnbindShouldWorkAndPasswordLoginRemains() throws Exception {
        String username = TestUsernames.unique("emuser");
        String token = register(username, "secret12");
        String email = username + "@example.com";

        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody(email, "BIND")))
                .andExpect(status().isOk());
        String bindCode = capturingEmailSender.getLastCode();
        assertThat(bindCode).matches("\\d{4}");

        mockMvc.perform(post("/api/v1/auth/email/bind")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bindBody(email, bindCode)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody(email, "LOGIN")))
                .andExpect(status().isOk());
        String loginCode = capturingEmailSender.getLastCode();
        assertThat(loginCode).matches("\\d{4}");

        mockMvc.perform(post("/api/v1/auth/loginByEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emailLoginBody(email, loginCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.token").isString());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("request", Map.of("username", username, "password", "secret12")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());

        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody(email, "UNBIND")))
                .andExpect(status().isOk());
        String unbindCode = capturingEmailSender.getLastCode();

        mockMvc.perform(post("/api/v1/auth/email/unbind")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unbindBody(unbindCode)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").doesNotExist());
    }

    @Test
    void occupiedEmailCannotBeBoundByAnotherUser() throws Exception {
        String owner = TestUsernames.unique("emown");
        String other = TestUsernames.unique("emoth");
        String ownerToken = register(owner, "secret12");
        String otherToken = register(other, "secret12");
        String email = owner + "@example.com";

        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody(email, "BIND")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/email/bind")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bindBody(email, capturingEmailSender.getLastCode())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody(email, "BIND")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("该邮箱已被绑定"));
    }

    @Test
    void unboundEmailLoginShouldFailWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email/sendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendCodeBody("nobody_" + TestUsernames.unique("x") + "@example.com", "LOGIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("邮箱或验证码错误"));
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

    private String sendCodeBody(String email, String purpose) throws Exception {
        return objectMapper.writeValueAsString(Map.of("request", Map.of("email", email, "purpose", purpose)));
    }

    private String bindBody(String email, String code) throws Exception {
        return objectMapper.writeValueAsString(Map.of("request", Map.of("email", email, "code", code)));
    }

    private String unbindBody(String code) throws Exception {
        return objectMapper.writeValueAsString(Map.of("request", Map.of("code", code)));
    }

    private String emailLoginBody(String email, String code) throws Exception {
        return objectMapper.writeValueAsString(Map.of("request", Map.of("email", email, "code", code)));
    }
}
