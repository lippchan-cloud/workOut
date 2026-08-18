package com.workout.modules.ai.domain;

/**
 * 分享建议分析状态（领域枚举）。
 * 不表示医疗结论，仅驱动报告页展示与异步任务。
 */
public enum AdviceStatus {
    /** 用户未绑定 API Key */
    NONE_KEY,
    /** 已提交，等待或正在生成 */
    PENDING,
    /** 已生成可展示文本 */
    READY,
    /** 限流或模型失败 */
    FAILED
}
