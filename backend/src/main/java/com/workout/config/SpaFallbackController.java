package com.workout.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA 深链回退（Web 配置层）。
 * 将前端路由转发到 index.html，避免刷新非根路径时落到 API 404；不处理 /api/**。
 */
@Controller
public class SpaFallbackController {

    private static final Logger log = LoggerFactory.getLogger(SpaFallbackController.class);

    /**
     * 将已知前端路由（含三级深链）转发为静态 index.html，供 React Router 接管。
     */
    @GetMapping({
        "/calendar",
        "/calendar/**",
        "/profile",
        "/profile/**",
        "/login",
        "/register",
        "/record",
        "/record/**",
        "/cms",
        "/cms/**",
        "/report",
        "/report/**"
    })
    public String forwardSpaRoutes() {
        // 关键入口：深链回退，避免被当作缺失 API
        log.info("[SPA托管] forwardSpaRoutes start target=index.html");
        log.info("[SPA托管] forwardSpaRoutes done");
        return "forward:/index.html";
    }
}
