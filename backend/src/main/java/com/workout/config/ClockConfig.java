package com.workout.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统时钟 Bean（基础设施）。
 * 默认 Asia/Shanghai，便于限流窗口与测试替换。
 */
@Configuration
public class ClockConfig {

    /**
     * 上海时区时钟。
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}
