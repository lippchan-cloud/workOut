package com.workout.modules.ai.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * DeepSeek HTTP 客户端（ACL 适配）。
 * 模型 deepseek-chat；base https://api.deepseek.com；日志不打印完整 apiKey。
 */
@Component
@ConditionalOnProperty(name = "workout.ai.deepseek.stub", havingValue = "false", matchIfMissing = true)
public class HttpDeepSeekClient implements DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(HttpDeepSeekClient.class);
    private static final String BASE = "https://api.deepseek.com";
    private static final String MODEL = "deepseek-chat";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * 注入 JSON 与 HTTP 客户端。
     */
    public HttpDeepSeekClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(String apiKey, String systemPrompt, String userPrompt) {
        log.info(
                "[DeepSeek] chat start model={}, keyMask={}, systemLen={}, userLen={}",
                MODEL,
                mask(apiKey),
                systemPrompt == null ? 0 : systemPrompt.length(),
                userPrompt == null ? 0 : userPrompt.length());
        try {
            Map<String, Object> body = Map.of(
                    "model",
                    MODEL,
                    "messages",
                    List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)),
                    "temperature",
                    0.4);
            // 组装 OpenAI 兼容请求体并 POST
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("[DeepSeek] chat failed httpStatus={}, keyMask={}", response.statusCode(), mask(apiKey));
                throw new IllegalStateException("DeepSeek HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("DeepSeek empty content");
            }
            log.info("[DeepSeek] chat done keyMask={}, contentLen={}", mask(apiKey), content.length());
            return content.trim();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[DeepSeek] chat failed keyMask={}, msg={}", mask(apiKey), ex.getMessage());
            throw new IllegalStateException("DeepSeek 调用失败", ex);
        }
    }

    /**
     * 掩码 key，仅留尾 4 位。
     */
    private static String mask(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }
}
