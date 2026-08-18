package com.workout.modules.ai.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 分享建议事件监听（应用层）。
 * AFTER_COMMIT 后再调异步服务，避免与创建事务缠在一起。
 */
@Component
public class ShareAdviceEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShareAdviceEventListener.class);

    private final ShareAdviceService shareAdviceService;

    /**
     * 注入建议服务。
     */
    public ShareAdviceEventListener(ShareAdviceService shareAdviceService) {
        this.shareAdviceService = shareAdviceService;
    }

    /**
     * 创建分享事务提交后触发异步生成。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShareAdviceRequested(ShareAdviceRequestedEvent event) {
        log.info(
                "[建议分析] listener start tokenPrefix={}, userId={}",
                tokenPrefix(event.getShareToken()),
                event.getUserId());
        // 走代理上的 @Async/@Transactional，禁止同类自调用
        shareAdviceService.generateAsync(event.getShareToken(), event.getUserId());
        log.info("[建议分析] listener handed-off tokenPrefix={}", tokenPrefix(event.getShareToken()));
    }

    private static String tokenPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.substring(0, Math.min(8, token.length())) + "...";
    }
}
