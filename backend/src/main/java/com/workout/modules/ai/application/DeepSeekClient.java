package com.workout.modules.ai.application;

/**
 * DeepSeek 聊天客户端（应用层端口）。
 * 实现可替换；测试使用 Stub，禁止打真实外网。
 */
public interface DeepSeekClient {

    /**
     * 调用 chat completions。
     *
     * @param apiKey     用户绑定的 DeepSeek key（调用方已脱敏日志）
     * @param systemPrompt 系统角色（智能生理科学家）
     * @param userPrompt   含 userId 与压缩上下文
     * @return 模型回复文本
     */
    String chat(String apiKey, String systemPrompt, String userPrompt);
}
