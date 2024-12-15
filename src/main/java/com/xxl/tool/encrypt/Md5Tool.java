package com.xxl.tool.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * md5 tool
 *
 * @author xuxueli 2024-12-15
 */
public abstract class Md5Tool {

	private static final String MD5_ALGORITHM_NAME = "MD5";

	private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * calculate md5 for input
	 *
	 * @param input
	 * @return
	 */
	public static String md5(String input) {
		return md5(input.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * calculate md5 for input
	 *
	 * @param input 输入字节数组
	 * @return MD5摘要的十六进制字符串
	 */
	public static String md5(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
			byte[] digest = md.digest(input);
			return HexTool.byteToHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("Md5Tool#md5 error, input:"+ input, e);
		}
	}

	/*private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = HEX_CHARS[v >>> 4];
			hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
		}
		return new String(hexChars);
	}*/

}
