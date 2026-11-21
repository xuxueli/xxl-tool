package com.xxl.tool.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Cookie 工具：基于 Servlet API 读写浏览器 Cookie
 *
 * <p>
 * 写入时对 value 做 UTF-8 URL 编码，读取时做对应解码；默认路径为根路径 {@code /}，
 * 且所有写入的 Cookie 均标记为 HttpOnly（不设置 Secure）。
 * </p>
 *
 * <p>
 * 本类无状态，方法线程安全性由传入的 Servlet 容器 request/response 决定。
 * </p>
 *
 * @author xuxueli 2015-12-12 18:01:06
 */
public class CookieTool {

	/**
	 * 默认缓存时间,单位/秒
 	 */
	private static final int COOKIE_MAX_AGE = Integer.MAX_VALUE;
	/**
	 * 保存路径,根路径
 	 */
	private static final String COOKIE_PATH = "/";

	/**
	 * add cookie
	 *
	 * @param response   HttpServletResponse，不能为 {@code null}
	 * @param key        Cookie 名称，需符合容器与浏览器的命名约束
	 * @param value      Cookie 值，写入前按 UTF-8 做 URL 编码
	 * @param domain     Cookie 域；为 {@code null} 时不设置 Domain（仅当前域生效）
	 * @param path       Cookie 路径，通常为 {@code /}
	 * @param maxAge     存活时间，单位：秒；>0 为存活秒数，=0 表示删除，<0 表示浏览器会话结束时销毁
	 * @param isHttpOnly 是否禁止 JavaScript 读取
	 */
	private static void set(HttpServletResponse response, String key, String value,
							String domain, String path, int maxAge, boolean isHttpOnly) {

		// encode value
        try {
			value = URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }

		// add cookie
        Cookie cookie = new Cookie(key, value);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(isHttpOnly);
		response.addCookie(cookie);
	}

	/**
	 * 新增 cookie（根路径、HttpOnly）
	 *
	 * @param response   HttpServletResponse，不能为 {@code null}
	 * @param key        Cookie 名称
	 * @param value      Cookie 值，按 UTF-8 做 URL 编码后写入
	 * @param ifRemember 	： true - 永不过期（maxAge 取 {@link Integer#MAX_VALUE}），false - 浏览器退出则销毁；
	 */
	public static void set(HttpServletResponse response, String key, String value, boolean ifRemember) {
		int age = ifRemember?COOKIE_MAX_AGE:-1;
		set(response, key, value, null, COOKIE_PATH, age, true);
	}

	/**
	 * 新增 cookie（根路径、HttpOnly）
	 *
	 * @param response   HttpServletResponse，不能为 {@code null}
	 * @param key        Cookie 名称
	 * @param value      Cookie 值，按 UTF-8 做 URL 编码后写入
	 * @param maxAge     存活时间，单位：秒；>0 为存活秒数，=0 表示删除，<0 表示浏览器会话结束时销毁
	 */
	public static void set(HttpServletResponse response, String key, String value, int maxAge) {
		set(response, key, value, null, COOKIE_PATH, maxAge, true);
	}

	/**
	 * 删除 cookie
	 *
	 * <p>
	 * 仅当请求中存在同名 Cookie 时才写入删除指令；删除时固定使用根路径 {@code /} 且不指定 Domain，
	 * 因此无法删除其他路径或域下写入的同名 Cookie。
	 * </p>
	 *
	 * @param request  HttpServletRequest，用于判断 Cookie 是否存在
	 * @param response HttpServletResponse，用于写入 maxAge=0 的删除 Cookie
	 * @param key      Cookie 名称
	 */
	public static void remove(HttpServletRequest request, HttpServletResponse response, String key) {
		Cookie cookie = get(request, key);
		if (cookie != null) {
			set(response, key, "", null, COOKIE_PATH, 0, true);
		}
	}

	/**
	 * get cookie
	 *
	 * @param request HttpServletRequest，不能为 {@code null}
	 * @param key     Cookie 名称
	 * @return 匹配的 Cookie；请求无 Cookie 或名称不存在时返回 {@code null}
	 */
	private static Cookie get(HttpServletRequest request, String key) {
		Cookie[] arr_cookie = request.getCookies();
		if (arr_cookie != null && arr_cookie.length > 0) {
			for (Cookie cookie : arr_cookie) {
				if (cookie.getName().equals(key)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 查询 cookie value
	 *
	 * <p>
	 * 按名称查找请求中的 Cookie 并按 UTF-8 做 URL 解码；未找到返回 {@code null}。
	 * </p>
	 *
	 * @param request HttpServletRequest，不能为 {@code null}
	 * @param key     Cookie 名称
	 * @return 解码后的 Cookie 值；不存在时返回 {@code null}
	 */
	public static String getValue(HttpServletRequest request, String key) {
		Cookie cookie = get(request, key);
		if (cookie == null) {
			return null;
		}

		// decode value
		String value = cookie.getValue();
		try {
			value = URLDecoder.decode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return value;
	}

}