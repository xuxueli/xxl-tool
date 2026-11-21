package com.xxl.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编解码工具，基于 JDK {@link java.util.Base64}
 *
 * <p>字符串统一按 UTF-8 编码；Base64 只是编码而非加密，不能用于保护敏感数据。
 * URL 安全变体使用 {@code -_/} 字符集且不带 {@code =} 填充；标准变体使用 {@code +/} 并保留填充。
 * 所有方法均为无状态静态方法，线程安全，不修改入参。</p>
 *
 * @author xuxueli 2025-04
 */
public class Base64Tool {


    // --------------------------------------- UrlEncoder withoutPadding ---------------------------------------

    /**
     * URL安全的Base64编码（无填充）
     * 1、示例：{@code "Hello+World/123?"} 编码为 {@code SGVsbG8rV29ybGQvMTIzPw}
     * 2、场景：JWT、URL参数、文件名
     *
     * @param data 待编码字符串，按 UTF-8 处理，不可为 {@code null}
     * @return URL 安全的 Base64 字符串（无填充）
     * @throws NullPointerException {@code data} 为 {@code null} 时抛出
     */
    public static String encodeUrlSafe(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL安全的Base64编码（无填充）
     *
     * @param data 待编码字节数组，不可为 {@code null}
     * @return URL 安全的 Base64 字符串（无填充）
     * @throws NullPointerException {@code data} 为 {@code null} 时抛出
     */
    public static String encodeUrlSafe(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }

    /**
     * URL安全的Base64解码
     *
     * @param data URL 安全的 Base64 字符串（可无填充），不可为 {@code null}
     * @return 解码后的字符串，按 UTF-8 还原
     * @throws IllegalArgumentException 输入不是合法的 URL 安全 Base64 时抛出
     * @throws NullPointerException     {@code data} 为 {@code null} 时抛出
     */
    public static String decodeUrlSafe(String data) {
        return new String(
                Base64.getUrlDecoder().decode(data),
                StandardCharsets.UTF_8
        );
    }

    /**
     * URL安全的Base64解码为字节数组
     *
     * @param data URL 安全的 Base64 字符串（可无填充），不可为 {@code null}
     * @return 解码后的原始字节
     * @throws IllegalArgumentException 输入不是合法的 URL 安全 Base64 时抛出
     * @throws NullPointerException     {@code data} 为 {@code null} 时抛出
     */
    public static byte[] decodeUrlSafeToBytes(String data) {
        return Base64.getUrlDecoder().decode(data);
    }


    // --------------------------------------- Standard  Encode ---------------------------------------

    /**
     * 标准Base64编码
     * 1、示例：{@code "Hello+World/123?"} 编码为 {@code SGVsbG8rV29ybGQvMTIzPw==}
     * 2、场景：普通字符串、二进制数据、邮件
     *
     * @param data 待编码字符串，按 UTF-8 处理，不可为 {@code null}
     * @return 标准 Base64 字符串（带填充）
     * @throws NullPointerException {@code data} 为 {@code null} 时抛出
     */
    public static String encodeStandard(String data) {
        return Base64.getEncoder()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准Base64编码
     *
     * @param data 待编码字节数组，不可为 {@code null}
     * @return 标准 Base64 字符串（带填充）
     * @throws NullPointerException {@code data} 为 {@code null} 时抛出
     */
    public static String encodeStandard(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 标准Base64解码
     *
     * @param data 标准 Base64 字符串，不可为 {@code null}
     * @return 解码后的字符串，按 UTF-8 还原
     * @throws IllegalArgumentException 输入不是合法的标准 Base64 时抛出
     * @throws NullPointerException     {@code data} 为 {@code null} 时抛出
     */
    public static String decodeStandard(String data) {
        return new String(
                Base64.getDecoder().decode(data),
                StandardCharsets.UTF_8
        );
    }

    /**
     * 标准Base64解码为字节数组
     *
     * @param data 标准 Base64 字符串，不可为 {@code null}
     * @return 解码后的原始字节
     * @throws IllegalArgumentException 输入不是合法的标准 Base64 时抛出
     * @throws NullPointerException     {@code data} 为 {@code null} 时抛出
     */
    public static byte[] decodeStandardToBytes(String data) {
        return Base64.getDecoder().decode(data);
    }

}