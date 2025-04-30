package com.xxl.tool.encrypt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * base64 tool
 *
 * @author xuxueli 2025-04
 */
public class Base64Tool {


    // --------------------------------------- UrlEncoder withoutPadding ---------------------------------------

    /**
     * URL安全的Base64编码（无填充）
     * 1、示例：SGVsbG8rV29ybGQvMTIzPw==
     * 2、场景：JWT、URL参数、文件名
     */
    public static String encodeUrlSafe(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL安全的Base64编码（无填充）
     */
    public static String encodeUrlSafe(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }

    /**
     * URL安全的Base64解码
     */
    public static String decodeUrlSafe(String data) {
        return new String(
                Base64.getUrlDecoder().decode(data),
                StandardCharsets.UTF_8
        );
    }

    /**
     * URL安全的Base64解码
     */
    public static byte[] decodeUrlSafeToBytes(String data) {
        return Base64.getUrlDecoder().decode(data);
    }


    // --------------------------------------- Standard  Encode ---------------------------------------

    /**
     * 标准Base64编码
     * 1、示例: SGVsbG8rV29ybGQvMTIzPw
     * 2、场景：普通字符串、二进制数据、邮件
     */
    public static String encodeStandard(String data) {
        return Base64.getEncoder()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准Base64编码
     */
    public static String encodeStandard(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 标准Base64解码
     */
    public static String decodeStandard(String data) {
        return new String(
                Base64.getDecoder().decode(data),
                StandardCharsets.UTF_8
        );
    }

    /**
     * 标准Base64解码
     */
    public static byte[] decodeStandardToBytes(String data) {
        return Base64.getDecoder().decode(data);
    }

}