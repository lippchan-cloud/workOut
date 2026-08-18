package com.workout.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.config.AdminProperties;
import com.workout.modules.ai.infrastructure.ApiKeyPoolEntity;
import com.workout.modules.ai.infrastructure.ApiKeyPoolRepository;
import com.workout.modules.ai.infrastructure.UserApiKeyEntity;
import com.workout.modules.ai.infrastructure.UserApiKeyRepository;
import com.workout.support.TestUsernames;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 密钥库表 + 新注册用户默认绑定（TDD）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiKeyPoolAssignTest {

    private static final String POOL_KEY = "sk-test-pool-key-abcd";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminProperties adminProperties;

    @Autowired
    private UserApiKeyRepository userApiKeyRepository;

    @Autowired
    private ApiKeyPoolRepository apiKeyPoolRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final List<Long> temporarilyDisabledPoolIds = new ArrayList<>();

    @AfterEach
    void resetAdmin() {
        adminProperties.setUsernames("");
        if (!temporarilyDisabledPoolIds.isEmpty()) {
            // 恢复本用例临时关掉的其它密钥，避免污染共享库
            transactionTemplate.executeWithoutResult(status -> {
                for (Long id : temporarilyDisabledPoolIds) {
                    apiKeyPoolRepository.findById(id).ifPresent(p -> {
                        p.setEnabled(true);
                        apiKeyPoolRepository.save(p);
                    });
                }
            });
            temporarilyDisabledPoolIds.clear();
        }
    }

    @Test
    void adminCanCreatePoolKeyAndNewUserGetsAssigned() throws Exception {
        String adminName = TestUsernames.unique("pooladm");
        adminProperties.setUsernames(adminName);
        String adminToken = register(adminName, "secret12").path("token").asText();

        MvcResult created = mockMvc.perform(post("/api/v1/admin/apiKeys/pool")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("request", Map.of("apiKey", POOL_KEY)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyMask").value("****abcd"))
                .andReturn();
        String createdBody = created.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(createdBody).doesNotContain(POOL_KEY);
        Long createdPoolId = objectMapper.readTree(createdBody).path("data").path("id").asLong();

        MvcResult poolList = mockMvc.perform(get("/api/v1/admin/apiKeys/pool")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andReturn();
        String poolBody = poolList.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(poolBody).contains("****abcd");
        assertThat(poolBody).doesNotContain(POOL_KEY);

        // 共享库里可能已有其它启用 key；只留本用例这把，才能断言默认绑到 ****abcd
        transactionTemplate.executeWithoutResult(status -> {
            for (ApiKeyPoolEntity p : apiKeyPoolRepository.findAll()) {
                if (!p.getId().equals(createdPoolId) && p.isEnabled()) {
                    p.setEnabled(false);
                    apiKeyPoolRepository.save(p);
                    temporarilyDisabledPoolIds.add(p.getId());
                }
            }
        });

        String newbie = TestUsernames.unique("pooluser");
        JsonNode data = register(newbie, "secret12");
        Long userId = data.path("userId").asLong();
        UserApiKeyEntity bound = userApiKeyRepository.findByUserId(userId).orElseThrow();
        assertThat(bound.getPoolId()).isEqualTo(createdPoolId);
        assertThat(bound.getKeyMask()).isEqualTo("****abcd");
        assertThat(bound.getApiKey()).isEqualTo(POOL_KEY);
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
