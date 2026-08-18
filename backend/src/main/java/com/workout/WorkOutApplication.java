package com.workout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * workOut 应用入口（启动层）。
 * 扫描 com.workout 下的 config / common / modules，不承载业务规则。
 */
@SpringBootApplication
public class WorkOutApplication {

    private static final Logger log = LoggerFactory.getLogger(WorkOutApplication.class);

    /**
     * 启动 Spring Boot 进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        log.info("[启动] WorkOutApplication.main start app=workout");
        SpringApplication.run(WorkOutApplication.class, args);
    }
}
