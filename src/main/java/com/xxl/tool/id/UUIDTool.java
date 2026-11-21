package com.xxl.tool.id;

import java.util.UUID;

/**
 * UUID 工具，基于 {@link UUID#randomUUID()} 生成版本 4（随机）UUID
 *
 * <p>标准形式为 36 位（含 4 个连字符），简单形式为 32 位（去掉连字符）。
 * UUID 基于 {@link java.security.SecureRandom} 生成，但仍是<b>概率唯一</b>而非绝对唯一。</p>
 *
 * <p>无状态静态方法，线程安全；不修改入参。</p>
 *
 * @author xuxueli 2025-06-29
 */
public class UUIDTool {

    /**
     * 生成标准 UUID 字符串
     *
     * @return 36 位 UUID，例如 {@code 550e8400-e29b-41d4-a716-446655440000}
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成不含连字符的简单 UUID 字符串
     *
     * @return 32 位十六进制 UUID
     */
    public static String getSimpleUUID() {
        return getUUID().replace("-", "");
    }

}
