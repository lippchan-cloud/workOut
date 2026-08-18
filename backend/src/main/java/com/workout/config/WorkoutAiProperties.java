package com.workout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI / DeepSeek 配置（配置层）。
 * apiKey 仅用于种子与运行；日志禁止打印全文。
 */
@ConfigurationProperties(prefix = "workout.ai")
public class WorkoutAiProperties {

    private final Async async = new Async();
    private final Deepseek deepseek = new Deepseek();

    /**
     * 异步相关。
     */
    public Async getAsync() {
        return async;
    }

    /**
     * DeepSeek 相关。
     */
    public Deepseek getDeepseek() {
        return deepseek;
    }

    /**
     * 异步执行开关。
     */
    public static class Async {
        /** true 时用同步执行器（测试）。 */
        private boolean sync;

        /**
         * 是否同步执行异步任务。
         */
        public boolean isSync() {
            return sync;
        }

        /**
         * 设置同步开关。
         */
        public void setSync(boolean sync) {
            this.sync = sync;
        }
    }

    /**
     * DeepSeek 客户端与种子。
     */
    public static class Deepseek {
        private boolean stub;
        private String apiKey = "";
        private String seedUsernames = "demo,lipp";

        /**
         * 是否使用 Stub（测试禁外网）。
         */
        public boolean isStub() {
            return stub;
        }

        /**
         * 设置 stub。
         */
        public void setStub(boolean stub) {
            this.stub = stub;
        }

        /**
         * 默认 / 环境变量注入的 API Key。
         */
        public String getApiKey() {
            return apiKey;
        }

        /**
         * 设置 API Key。
         */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * 启动时把 key 赋给这些用户名（逗号分隔）。
         */
        public String getSeedUsernames() {
            return seedUsernames;
        }

        /**
         * 设置种子用户名列表。
         */
        public void setSeedUsernames(String seedUsernames) {
            this.seedUsernames = seedUsernames;
        }
    }
}
