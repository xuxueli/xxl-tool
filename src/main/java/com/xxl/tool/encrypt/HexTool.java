package com.xxl.tool.encrypt;

import java.nio.charset.StandardCharsets;

/**
 * hex tool
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

    public static int getDec(int index) {
        // Fast for correct values, slower for incorrect ones
        try {
            return DEC[index - '0'];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }


    public static byte getHex(int index) {
        return HEX[index];
    }


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
     * byte to hex
     *
     * @param input
     * @return
     */
    public static String toHex(String input) {
        return byteToHex(input.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * byte to hex
     *
     * @param bytes
     * @return
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
     * hex to byte
     *
     * @param input
     * @return
     */
    public static String fromHex(String input) {
        return new String(hexToByte(input), StandardCharsets.UTF_8);
    }

    /**
     * hex to byte
     *
     * @param input
     * @return
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
