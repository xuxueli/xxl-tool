package com.xxl.tool.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * sha256 tool
 *
 * @author xuxueli 2025-07-27
 */
public class SHA256Tool {

    private static final String SHA_256_ALGORITHM_NAME = "SHA-256";

    /**
     * calculate sha256 for input
     *
     * @param input
     * @return
     */
    public static String sha256(String input) {
        return sha256(input.getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * calculate sha256 for input
     *
     * @param input	the input string
     * @param salt	the salt for sha256
     * @return return sha256 string of input
     */
    public static String sha256(String input, String salt) {
        byte[] saltBytes = salt!=null ?
                salt.getBytes(StandardCharsets.UTF_8) :
                null;
        return sha256(input.getBytes(StandardCharsets.UTF_8), saltBytes);
    }

    /**
     * calculate sha256 for input
     *
     * @param input	the input byte array
     * @param salt	the salt for sha256
     * @return return sha256 string of input
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