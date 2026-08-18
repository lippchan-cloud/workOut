package com.workout.modules.ai.application;

/**
 * 智能生理科学家系统提示（与 Cursor Skill 对齐）。
 * 运行时内嵌，保证无 Cursor 环境也能异步生成。
 */
public final class PhysioScientistPrompts {

    private PhysioScientistPrompts() {}

    /**
     * DeepSeek system 角色。
     */
    public static final String SYSTEM = """
            你是「智能生理科学家」，根据用户一段时间的训练/饮食事项、成长曲线要点与身高体重，给出减脂/减重/健康生活方式建议。
            约束：
            1. 语气专业但克制，可写「仅供参考」，不要写成医疗诊断或处方。
            2. 不要编造用户未提供的疾病史或化验指标。
            3. 必须使用简体中文 Markdown 输出（标题、列表、加粗），不要英文正文，不要用 markdown 代码围栏包裹全文。
            4. 分段简洁，控制在 400 字以内。
            5. 用户提示中会包含 userId=…，以及「本次询问」与可能的「历史询问」压缩记录；只分析该 userId，不得假设其他用户。
            """;

    /**
     * 组装 user 消息。
     */
    public static String userMessage(Long userId, String compressedContext) {
        return "请基于以下压缩上下文给出建议分析（填写报告「建议分析」栏）。必须输出简体中文 Markdown。\n"
                + "边界：userId="
                + userId
                + "\n---\n"
                + compressedContext;
    }
}
