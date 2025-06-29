package com.xxl.tool.id;

import com.xxl.tool.core.StringTool;

import java.util.Random;

/**
 * random id tool
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
     * get random digits, default length 20
     */
    public static String getDigitId() {
        return getDigitId(DEFAULT_LENGTH);
    }

    /**
     * get random digits
     *
     * @param length
     * @return
     */
    public static String getDigitId(int length) {
        return getRandomId(DIGITS, length);
    }

    /**
     * get random lowercase letters, default length 20
     *
     * @return
     */
    public static String getLowercaseId() {
        return getLowercaseId(DEFAULT_LENGTH);
    }

    /**
     * get random lowercase letters
     *
     * @param length
     * @return
     */
    public static String getLowercaseId(int length) {
        return getRandomId(LOWERCASE, length);
    }

    /**
     * get random uppercase letters, default length 20
     *
     * @return
     */
    public static String getUppercaseId() {
        return getUppercaseId(DEFAULT_LENGTH);
    }

    /**
     * get random uppercase letters
     *
     * @param length
     * @return
     */
    public static String getUppercaseId(int length) {
        return getRandomId(UPPERCASE, length);
    }


    /**
     * get random alpha numeric, default length 20
     *
     * @return
     */
    public static String getAlphaNumeric() {
        return getAlphaNumeric(DEFAULT_LENGTH);
    }

    /**
     * get random alpha numeric
     *
     * @param length
     * @return
     */
    public static String getAlphaNumeric(int length) {
        String alphaNumeric = DIGITS + LOWERCASE + UPPERCASE;
        return getRandomId(alphaNumeric, length);
    }

    /**
     * get random alpha numeric with special chars, default length is 20
     *
     * @return
     */
    public static String getAlphaNumericWithSpecial() {
        return getAlphaNumericWithSpecial(DEFAULT_LENGTH);
    }

    /**
     * get random alpha numeric with special chars
     *
     * @param length
     * @return
     */
    public static String getAlphaNumericWithSpecial(int length) {
        String allChars = DIGITS + LOWERCASE + UPPERCASE + SPECIAL_CHARS;
        return getRandomId(allChars, length);
    }

    /**
     * get random id
     *
     * @param length
     * @return
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
