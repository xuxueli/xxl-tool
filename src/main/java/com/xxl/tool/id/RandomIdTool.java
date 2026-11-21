package com.xxl.tool.id;

import com.xxl.tool.core.StringTool;

import java.util.Random;

/**
 * 随机字符串 ID 工具
 *
 * <p>支持数字、小写字母、大写字母、字母数字及含特殊字符等字符集；默认长度 20，
 * 允许长度范围 1-1000。</p>
 *
 * <p><b>随机性与唯一性：</b>内部使用 {@link java.util.Random}（非加密安全），
 * 生成结果<b>不保证唯一</b>，不可用于令牌、密码、验证码等安全场景；需要抗预测随机数时
 * 请使用 {@link java.security.SecureRandom}。每次调用新建 {@link Random}，方法无共享状态，
 * 可并发调用。</p>
 *
 * @author xuxueli 2025-06-29
 */
public class RandomIdTool {

    private static final int DEFAULT_LENGTH = 20;

    private static final String DIGITS = "0123456789";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?/";

    /**
     * 生成随机数字 ID，默认长度 20
     *
     * @return 由 0-9 组成的随机字符串
     */
    public static String getDigitId() {
        return getDigitId(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机数字 ID
     *
     * @param length 长度，有效范围 1-1000
     * @return 由 0-9 组成的随机字符串
     * @throws IllegalArgumentException 长度不在 1-1000 时抛出
     */
    public static String getDigitId(int length) {
        return getRandomId(DIGITS, length);
    }

    /**
     * 生成随机小写字母 ID，默认长度 20
     *
     * @return 由 a-z 组成的随机字符串
     */
    public static String getLowercaseId() {
        return getLowercaseId(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机小写字母 ID
     *
     * @param length 长度，有效范围 1-1000
     * @return 由 a-z 组成的随机字符串
     * @throws IllegalArgumentException 长度不在 1-1000 时抛出
     */
    public static String getLowercaseId(int length) {
        return getRandomId(LOWERCASE, length);
    }

    /**
     * 生成随机大写字母 ID，默认长度 20
     *
     * @return 由 A-Z 组成的随机字符串
     */
    public static String getUppercaseId() {
        return getUppercaseId(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机大写字母 ID
     *
     * @param length 长度，有效范围 1-1000
     * @return 由 A-Z 组成的随机字符串
     * @throws IllegalArgumentException 长度不在 1-1000 时抛出
     */
    public static String getUppercaseId(int length) {
        return getRandomId(UPPERCASE, length);
    }


    /**
     * 生成随机字母数字 ID，默认长度 20
     *
     * @return 由 0-9、a-z、A-Z 组成的随机字符串
     */
    public static String getAlphaNumeric() {
        return getAlphaNumeric(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机字母数字 ID
     *
     * @param length 长度，有效范围 1-1000
     * @return 由 0-9、a-z、A-Z 组成的随机字符串
     * @throws IllegalArgumentException 长度不在 1-1000 时抛出
     */
    public static String getAlphaNumeric(int length) {
        String alphaNumeric = DIGITS + LOWERCASE + UPPERCASE;
        return getRandomId(alphaNumeric, length);
    }

    /**
     * 生成含特殊字符的随机 ID，默认长度 20
     *
     * @return 由字母、数字及 {@code !@#$%^&*()-_=+[]{}|;:,.<>?/} 组成的随机字符串
     */
    public static String getAlphaNumericWithSpecial() {
        return getAlphaNumericWithSpecial(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的含特殊字符随机 ID
     *
     * @param length 长度，有效范围 1-1000
     * @return 由字母、数字及特殊字符组成的随机字符串
     * @throws IllegalArgumentException 长度不在 1-1000 时抛出
     */
    public static String getAlphaNumericWithSpecial(int length) {
        String allChars = DIGITS + LOWERCASE + UPPERCASE + SPECIAL_CHARS;
        return getRandomId(allChars, length);
    }

    /**
     * 从指定字符集中生成随机字符串
     *
     * @param characters 候选字符集，不能为空白
     * @param length     生成长度，有效范围 1-1000
     * @return 随机字符串，每个字符独立从 {@code characters} 中抽取
     * @throws IllegalArgumentException 长度不在 1-1000，或字符集为空白时抛出
     */
    public static String getRandomId(String characters, int length) {

        // valid
        if (!(length>=1 && length<=1000)) {
            throw new IllegalArgumentException("random length must be between 1 and 1000.");
        }
        if (StringTool.isBlank(characters)) {
            throw new IllegalArgumentException("random characters can't be empty.");
        }

        // generate
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

}
