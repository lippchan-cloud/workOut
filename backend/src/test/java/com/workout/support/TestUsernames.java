package com.workout.support;

import java.util.UUID;

/**
 * 真实库集成测试用的用户名工厂。
 * 通过短 UUID 后缀避免固定用户名污染 / 重复注册失败（username 最长 32）。
 */
public final class TestUsernames {

    private TestUsernames() {}

    /**
     * @param prefix 可读前缀（建议 ≤20 字符）
     * @return 形如 {@code prefix_xxxxxxxx} 的唯一用户名
     */
    public static String unique(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String name = prefix + "_" + suffix;
        return name.length() <= 32 ? name : name.substring(0, 32);
    }
}
