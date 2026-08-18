package com.workout.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步任务配置（基础设施）。
 * 测试 profile 用同步执行器，保证 AFTER_COMMIT 监听可立即跑完。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * 生产异步线程池。
     */
    @Bean(name = "taskExecutor")
    @ConditionalOnProperty(name = "workout.ai.async.sync", havingValue = "false", matchIfMissing = true)
    public Executor taskExecutor() {
        log.info("[异步] create ThreadPoolTaskExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("workout-ai-");
        executor.initialize();
        return executor;
    }

    /**
     * 测试同步执行器。
     */
    @Bean(name = "taskExecutor")
    @ConditionalOnProperty(name = "workout.ai.async.sync", havingValue = "true")
    public Executor syncTaskExecutor() {
        log.info("[异步] create SyncTaskExecutor for tests");
        return new SyncTaskExecutor();
    }
}
