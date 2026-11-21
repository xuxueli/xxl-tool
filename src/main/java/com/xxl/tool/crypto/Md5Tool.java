package com.xxl.tool.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * MD5 摘要工具，输出 32 位小写十六进制字符串
 *
 * <p><b>用途：</b>仅用于数据完整性校验、缓存键、非安全场景的指纹计算。</p>
 *
 * <p><b>安全警告：</b>MD5 已被证明存在碰撞攻击，<b>不可用于密码存储或签名</b>；
 * 密码场景请使用 bcrypt / PBKDF2 / Argon2 等专用 KDF，完整性场景建议使用 SHA-256。
 * 带 salt 的重载会把 salt 字节先于输入写入摘要，salt 不能替代 KDF 的慢哈希。</p>
 *
 * <p>所有方法为无状态静态方法，线程安全，不修改入参（salt 为 {@code null} 时不加盐）。</p>
 *
 * @author xuxueli 2024-12-15
 */
public class Md5Tool {

	private static final String MD5_ALGORITHM_NAME = "MD5";

	//private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * 计算字符串的 MD5（不加盐）
	 *
	 * @param input 待摘要字符串，按 UTF-8 处理，不可为 {@code null}
	 * @return 32 位小写十六进制摘要
	 * @throws NullPointerException {@code input} 为 {@code null} 时抛出
	 */
	public static String md5(String input) {
		return md5(input.getBytes(StandardCharsets.UTF_8), null);
	}

	/**
	 * 计算字符串的 MD5（可加盐）
	 *
	 * @param input 待摘要字符串，按 UTF-8 处理，不可为 {@code null}
	 * @param salt  盐值，按 UTF-8 处理；{@code null} 表示不加盐
	 * @return 32 位小写十六进制摘要
	 * @throws NullPointerException {@code input} 为 {@code null} 时抛出
	 */
	public static String md5(String input, String salt) {
		byte[] saltBytes = salt!=null ?
				salt.getBytes(StandardCharsets.UTF_8) :
				null;
		return md5(input.getBytes(StandardCharsets.UTF_8), saltBytes);
	}

	/**
	 * 计算字节数组的 MD5（可加盐）
	 *
	 * @param input 待摘要字节数组，不可为 {@code null}
	 * @param salt  盐值字节数组；{@code null} 表示不加盐
	 * @return 32 位小写十六进制摘要
	 * @throws IllegalStateException 摘要算法不可用时抛出（JDK 环境正常不会发生）
	 * @throws NullPointerException  {@code input} 为 {@code null} 时抛出
	 */
	public static String md5(byte[] input, byte[] salt) {
		try {
			MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
			if (salt != null) {
				md.update(salt);
			}
			byte[] digest = md.digest(input);
			return HexTool.byteToHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("Md5Tool#md5 error, input:"+ Arrays.toString(input), e);
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
