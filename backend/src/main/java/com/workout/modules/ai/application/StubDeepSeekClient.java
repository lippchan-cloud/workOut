package com.workout.modules.ai.application;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 可录制的 DeepSeek Stub（测试/本地）。
 * 默认返回固定文案；可切换为抛错；计数供断言未超限外呼。绝不发起真实 HTTP。
 */
@Component
@ConditionalOnProperty(name = "workout.ai.deepseek.stub", havingValue = "true")
public class StubDeepSeekClient implements DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(StubDeepSeekClient.class);

    private final AtomicInteger invokeCount = new AtomicInteger();
    private final AtomicBoolean failNext = new AtomicBoolean(false);
    private final AtomicReference<String> lastUserPrompt = new AtomicReference<>("");
    private final AtomicReference<String> fixedReply =
            new AtomicReference<>("保持规律作息与均衡饮食，循序渐进增加消耗，建议仅供参考。");

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(String apiKey, String systemPrompt, String userPrompt) {
        int n = invokeCount.incrementAndGet();
        lastUserPrompt.set(userPrompt == null ? "" : userPrompt);
        log.info(
                "[DeepSeekStub] chat start n={}, keyMask={}, userContainsUserId={}",
                n,
                mask(apiKey),
                userPrompt != null && userPrompt.contains("userId="));
        if (failNext.compareAndSet(true, false)) {
            log.error("[DeepSeekStub] chat failed forced");
            throw new IllegalStateException("stub forced failure");
        }
        String reply = fixedReply.get();
        log.info("[DeepSeekStub] chat done n={}, replyLen={}", n, reply.length());
        return reply;
    }

    /**
     * 测试重置计数与行为。
     */
    public void reset() {
        invokeCount.set(0);
        failNext.set(false);
        lastUserPrompt.set("");
        fixedReply.set("保持规律作息与均衡饮食，循序渐进增加消耗，建议仅供参考。");
    }

    /**
     * 下次调用失败。
     */
    public void failNext() {
        failNext.set(true);
    }

    /**
     * 设置固定回复。
     */
    public void setFixedReply(String text) {
        fixedReply.set(text);
    }

    /**
     * 已调用次数。
     */
    public int getInvokeCount() {
        return invokeCount.get();
    }

    /**
     * 最近一次 user prompt（断言 userId 边界）。
     */
    public String getLastUserPrompt() {
        return lastUserPrompt.get();
    }

    private static String mask(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }
}
