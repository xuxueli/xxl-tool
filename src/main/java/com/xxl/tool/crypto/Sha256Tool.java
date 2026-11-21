package com.xxl.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * SHA-256 摘要工具，输出 64 位小写十六进制字符串
 *
 * <p><b>用途：</b>数据完整性校验、内容指纹、HMAC 类自定义拼接（需自行设计密钥方案）。</p>
 *
 * <p><b>安全警告：</b>SHA-256 是快速哈希，<b>不可直接用于密码存储</b>；密码场景请使用
 * bcrypt / PBKDF2 / Argon2 等带盐慢哈希 KDF。带 salt 的重载只是把 salt 字节先于输入写入摘要，
 * 不提供密钥保密性。</p>
 *
 * <p>所有方法为无状态静态方法，线程安全，不修改入参（salt 为 {@code null} 时不加盐）。</p>
 *
 * @author xuxueli 2025-07-27
 */
public class Sha256Tool {

    private static final String SHA_256_ALGORITHM_NAME = "SHA-256";

    /**
     * 计算字符串的 SHA-256（不加盐）
     *
     * @param input 待摘要字符串，按 UTF-8 处理，不可为 {@code null}
     * @return 64 位小写十六进制摘要
     * @throws NullPointerException {@code input} 为 {@code null} 时抛出
     */
    public static String sha256(String input) {
        return sha256(input.getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * 计算字符串的 SHA-256（可加盐）
     *
     * @param input 待摘要字符串，按 UTF-8 处理，不可为 {@code null}
     * @param salt  盐值，按 UTF-8 处理；{@code null} 表示不加盐
     * @return 64 位小写十六进制摘要
     * @throws NullPointerException {@code input} 为 {@code null} 时抛出
     */
    public static String sha256(String input, String salt) {
        byte[] saltBytes = salt!=null ?
                salt.getBytes(StandardCharsets.UTF_8) :
                null;
        return sha256(input.getBytes(StandardCharsets.UTF_8), saltBytes);
    }

    /**
     * 计算字节数组的 SHA-256（可加盐）
     *
     * @param input 待摘要字节数组，不可为 {@code null}
     * @param salt  盐值字节数组；{@code null} 表示不加盐
     * @return 64 位小写十六进制摘要
     * @throws IllegalStateException 摘要算法不可用时抛出（JDK 环境正常不会发生）
     * @throws NullPointerException  {@code input} 为 {@code null} 时抛出
     */
    public static String sha256(byte[] input, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256_ALGORITHM_NAME);
            if (salt != null) {
                md.update(salt);
            }
            byte[] digest = md.digest(input);
            return HexTool.byteToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA256Tool#sha256 error, input:"+ Arrays.toString(input), e);
        }
    }

}