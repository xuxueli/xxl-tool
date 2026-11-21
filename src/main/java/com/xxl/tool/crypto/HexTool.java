package com.xxl.tool.crypto;

import java.nio.charset.StandardCharsets;

/**
 * 十六进制编解码工具
 *
 * <p>字符串按 UTF-8 转字节后再编码；输出统一为小写十六进制。十六进制只是编码不是加密。
 * 所有方法为无状态静态方法，线程安全，不修改入参。</p>
 *
 * @author xuxueli 2024-12-15
 */
public final class HexTool {

    // ---------------------- constants ----------------------

    /**
     * Table for HEX to DEC byte translation.
     */
    private static final int[] DEC = { 00, 01, 02, 03, 04, 05, 06, 07, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13,
            14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, };


    /**
     * Table for DEC to HEX byte translation.
     */
    private static final byte[] HEX =
            { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                    (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };


    /**
     * Table for byte to hex string translation.
     */
    private static final char[] hex = "0123456789abcdef".toCharArray();


    // ---------------------- Static Methods ----------------------

    /**
     * 将十六进制字符转换为十进制数值
     *
     * @param index 十六进制字符（{@code 0-9}、{@code a-f}、{@code A-F}）
     * @return 对应的 0-15 数值；非十六进制字符返回 {@code -1}
     */
    public static int getDec(int index) {
        // Fast for correct values, slower for incorrect ones
        try {
            return DEC[index - '0'];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }


    /**
     * 将 0-15 的数值转换为十六进制字符
     *
     * @param index 数值，有效范围 0-15
     * @return 对应的小写十六进制字符
     * @throws ArrayIndexOutOfBoundsException {@code index} 超出 0-15 时抛出
     */
    public static byte getHex(int index) {
        return HEX[index];
    }


    /**
     * 将 char 编码为 4 位十六进制字符串
     *
     * @param c 待编码字符
     * @return 固定 4 位的小写十六进制字符串
     */
    public static String toHexString(char c) {
        // 2 bytes / 4 hex digits
        StringBuilder sb = new StringBuilder(4);

        sb.append(hex[(c & 0xf000) >> 12]);
        sb.append(hex[(c & 0x0f00) >> 8]);

        sb.append(hex[(c & 0xf0) >> 4]);
        sb.append(hex[(c & 0x0f)]);

        return sb.toString();
    }


    // ---------------------- tool ----------------------

    /**
     * 字符串转十六进制
     *
     * @param input 待编码字符串，按 UTF-8 处理，不可为 {@code null}
     * @return 小写十六进制字符串
     * @throws NullPointerException {@code input} 为 {@code null} 时抛出
     */
    public static String toHex(String input) {
        return byteToHex(input.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 字节数组转十六进制
     *
     * @param bytes 待编码字节数组，可为 {@code null}
     * @return 小写十六进制字符串；入参为 {@code null} 时返回 {@code null}
     */
    public static String byteToHex(byte[] bytes) {
        if (null == bytes) {
            return null;
        }

        StringBuilder sb = new StringBuilder(bytes.length << 1);
        for (byte aByte : bytes) {
            sb.append(hex[(aByte & 0xf0) >> 4]).append(hex[(aByte & 0x0f)]);
        }

        return sb.toString();
    }

    /**
     * 十六进制转字符串
     *
     * @param input 十六进制字符串，可为 {@code null}
     * @return 按 UTF-8 还原的字符串；入参为 {@code null} 时返回 {@code null}
     * @throws IllegalArgumentException 长度为奇数或包含非十六进制字符时抛出
     */
    public static String fromHex(String input) {
        return new String(hexToByte(input), StandardCharsets.UTF_8);
    }

    /**
     * 十六进制转字节数组
     *
     * @param input 十六进制字符串，可为 {@code null}
     * @return 解码后的字节数组；入参为 {@code null} 时返回 {@code null}
     * @throws IllegalArgumentException 长度为奇数或包含非十六进制字符时抛出
     */
    public static byte[] hexToByte(String input) {
        if (input == null) {
            return null;
        }

        if ((input.length() & 1) == 1) {
            throw new IllegalArgumentException("Odd number of characters");
        }

        char[] inputChars = input.toCharArray();
        byte[] result = new byte[input.length() >> 1];
        for (int i = 0; i < result.length; i++) {
            int upperNibble = getDec(inputChars[2 * i]);
            int lowerNibble = getDec(inputChars[2 * i + 1]);
            if (upperNibble < 0 || lowerNibble < 0) {
                throw new IllegalArgumentException("None hex character");
            }
            result[i] = (byte) ((upperNibble << 4) + lowerNibble);
        }
        return result;
    }

}
