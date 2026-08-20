package com.workout.modules.auth.infrastructure;

/**
 * 验证码邮件中文提示词（基础设施层）。
 * 只组装主题与正文给收件人阅读；不负责投递，也不写入授权码。
 */
public final class VerificationEmailCopy {

    /**
     * 禁止实例化：文案只通过静态方法读取。
     */
    private VerificationEmailCopy() {
    }

    /**
     * 按用途返回中文邮件主题（不含验证码）。
     *
     * @param purpose BIND / UNBIND / LOGIN
     * @return 中文主题
     */
    public static String subject(String purpose) {
        return switch (normalize(purpose)) {
            case "BIND" -> "workOut 绑定邮箱验证码";
            case "UNBIND" -> "workOut 解绑邮箱验证码";
            case "LOGIN" -> "workOut 登录验证码";
            default -> "workOut 验证码";
        };
    }

    /**
     * 按用途返回中文正文，内含 4 位验证码与操作说明。
     *
     * @param purpose BIND / UNBIND / LOGIN
     * @param code    明文 4 位码，仅写入邮件正文
     * @return 中文正文
     */
    public static String body(String purpose, String code) {
        return "您好，\n\n"
                + "您正在 workOut 进行「" + actionLabel(purpose) + "」操作。"
                + "请使用以下 4 位验证码完成验证，不要将验证码告知他人。\n\n"
                + "验证码：" + code + "\n\n"
                + "该验证码 10 分钟内有效。\n"
                + "如非本人操作，请忽略本邮件。";
    }

    /**
     * 将用途标签转成给用户看的中文操作名。
     *
     * @param purpose BIND / UNBIND / LOGIN
     * @return 中文操作名
     */
    private static String actionLabel(String purpose) {
        return switch (normalize(purpose)) {
            case "BIND" -> "绑定邮箱";
            case "UNBIND" -> "解绑邮箱";
            case "LOGIN" -> "邮箱登录";
            default -> "身份验证";
        };
    }

    /**
     * 规范化用途标签，避免空指针与大小写差异。
     *
     * @param purpose 原始用途
     * @return 大写用途，空则空串
     */
    private static String normalize(String purpose) {
        return purpose == null ? "" : purpose.trim().toUpperCase();
    }
}
