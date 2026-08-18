package com.workout.modules.ai.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 分享建议异步门面（应用层）。
 * 仅负责异步调度；写库交给 ShareAdviceWorker 新事务。
 */
@Service
public class ShareAdviceService {

    private static final Logger log = LoggerFactory.getLogger(ShareAdviceService.class);
    public static final String MSG_NO_KEY = "未配置 API Key";
    public static final String MSG_RATE = "调用过于频繁，请稍后再试（仅供参考占位）";
    public static final String MSG_FAIL = "建议生成失败，请稍后重试（仅供参考）";

    private final ShareAdviceWorker shareAdviceWorker;

    /**
     * 注入 Worker。
     */
    public ShareAdviceService(ShareAdviceWorker shareAdviceWorker) {
        this.shareAdviceWorker = shareAdviceWorker;
    }

    /**
     * 异步入口。
     */
    @Async("taskExecutor")
    public void generateAsync(String token, Long expectedUserId) {
        log.info(
                "[建议分析] generateAsync start tokenPrefix={}, expectedUserId={}",
                tokenPrefix(token),
                expectedUserId);
        try {
            shareAdviceWorker.generateInNewTx(token, expectedUserId);
            log.info("[建议分析] generateAsync done tokenPrefix={}", tokenPrefix(token));
        } catch (Exception ex) {
            log.error(
                    "[建议分析] generateAsync failed tokenPrefix={}, msg={}",
                    tokenPrefix(token),
                    ex.getMessage());
        }
    }

    private static String tokenPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.substring(0, Math.min(8, token.length())) + "...";
    }
}
