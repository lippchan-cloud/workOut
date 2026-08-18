package com.workout.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 管理员引导配置（配置层）。
 * 从 workout.admin.usernames 读取逗号分隔用户名；匹配则在注册/登录时提升为 ADMIN。不引入完整 IAM。
 */
@ConfigurationProperties(prefix = "workout.admin")
public class AdminProperties {

    private static final Logger log = LoggerFactory.getLogger(AdminProperties.class);

    private String usernames = "";

    /**
     * 读取引导管理员用户名列表（逗号分隔原文）。
     */
    public String getUsernames() {
        return usernames;
    }

    /**
     * 设置引导管理员用户名列表，测试可临时改写。
     */
    public void setUsernames(String usernames) {
        this.usernames = usernames == null ? "" : usernames;
    }

    /**
     * 判断用户名是否属于引导管理员。
     */
    public boolean isBootstrapAdmin(String username) {
        if (username == null || username.isBlank() || usernames.isBlank()) {
            log.info("[管理员引导] isBootstrapAdmin miss username={}, configuredBlank={}", username, usernames.isBlank());
            return false;
        }
        Set<String> names = Arrays.stream(usernames.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        boolean match = names.contains(username);
        log.info("[管理员引导] isBootstrapAdmin username={}, match={}, size={}", username, match, names.size());
        return match;
    }
}
