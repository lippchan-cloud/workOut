package com.workout.modules.ai.application;

/**
 * 分享创建后触发建议生成的领域事件（应用层消息）。
 * 在事务提交后消费，避免阻塞 POST share。
 */
public class ShareAdviceRequestedEvent {

    private final String shareToken;
    private final Long userId;

    /**
     * @param shareToken 公开 token
     * @param userId     所有者
     */
    public ShareAdviceRequestedEvent(String shareToken, Long userId) {
        this.shareToken = shareToken;
        this.userId = userId;
    }

    /**
     * 分享 token。
     */
    public String getShareToken() {
        return shareToken;
    }

    /**
     * 用户 id。
     */
    public Long getUserId() {
        return userId;
    }
}
