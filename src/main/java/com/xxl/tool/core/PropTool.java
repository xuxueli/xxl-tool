package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Properties 工具：从类路径资源或文件加载 UTF-8 编码的 properties，并提供常用类型读取方法。
 *
 * <p>加载方法在资源不存在或读取失败时返回空（或已加载部分）的 {@link Properties}，不会返回 {@code null}，
 * 也不会向调用方抛出 IO 异常。读取方法对缺失的 key 按各方法说明处理。
 *
 * <p>所有方法均为静态方法，不修改入参；类无状态（仅持有日志对象），线程安全。
 *
 * @author xuxueli 2015-6-22 22:36:46
 */
public class PropTool {
	private static Logger logger = LoggerFactory.getLogger(PropTool.class);


	// ---------------------- load prop value ----------------------

	/**
	 * 从类路径资源加载 properties
	 *
	 * <p>通过当前线程上下文类加载器读取类路径资源，按 UTF-8 解析。资源路径为 {@code null} 或空白、
	 * 资源不存在时返回空 {@link Properties}；读取异常会记录 error 日志并返回已加载内容（可能为空）。
	 *
	 * @param resourcePath 类路径资源路径，可为 {@code null} 或空白
	 * @return 加载结果，不会返回 {@code null}
	 */
	public static Properties loadProp(String resourcePath) {
		Properties prop = new Properties();
		if (resourcePath == null || resourcePath.trim().isEmpty()) {
			return prop;
		}

		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
			if (input != null) {
				prop.load(new InputStreamReader(input, StandardCharsets.UTF_8));
			}
		} catch (Exception e) {
			logger.error("PropTool loadProp error:", e);
		}
		return prop;
	}

	/**
	 * 从文件路径加载 properties
	 *
	 * <p>按文件系统路径读取，按 UTF-8 解析。文件名为 {@code null} 或空白、文件不存在时返回空
	 * {@link Properties}；读取异常会记录 error 日志并返回已加载内容（可能为空）。
	 *
	 * @param fileName 文件路径，可为 {@code null} 或空白
	 * @return 加载结果，不会返回 {@code null}
     */
	public static Properties loadFileProp(String fileName) {
		Properties prop = new Properties();
		if (fileName == null || fileName.trim().isEmpty()) {
			return prop;
		}

		Path path = Paths.get(fileName);
		if (!Files.exists(path)) {
			return prop;
		}

		try (InputStream input = Files.newInputStream(path)) {
			prop.load(new InputStreamReader(input, StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("PropTool loadFileProp error:", e);
		}
		return prop;
	}

	// ---------------------- read prop value ----------------------

	/**
	 * 读取字符串值
	 *
	 * @param prop 属性集合，不可为 {@code null}
	 * @param key  属性名，可为 {@code null}
	 * @return 属性值；key 不存在时返回 {@code null}，值不做 trim 处理
	 * @throws NullPointerException 当 {@code prop} 为 {@code null} 时抛出
	 */
	public static String getString(Properties prop, String key) {
		return prop.getProperty(key);
	}

	/**
	 * 读取字符串值，缺失时返回默认值
	 *
	 * @param prop         属性集合，不可为 {@code null}
	 * @param key          属性名，可为 {@code null}
	 * @param defaultValue 属性缺失时返回的默认值，可为 {@code null}
	 * @return 属性值；key 不存在时返回 {@code defaultValue}（空串不属于缺失，会原样返回）
	 * @throws NullPointerException 当 {@code prop} 为 {@code null} 时抛出
	 */
	public static String getString(Properties prop, String key, String defaultValue) {
		String value = getString(prop, key);
		if (value==null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 读取 int 值
	 *
	 * @param prop 属性集合，不可为 {@code null}
	 * @param key  属性名，可为 {@code null}
	 * @return 解析后的 int 值
	 * @throws NullPointerException     当 {@code prop} 为 {@code null} 时抛出
	 * @throws NumberFormatException 当 key 不存在（值为 {@code null}）或值不是合法整数时抛出
	 */
	public static int getInt(Properties prop, String key) {
		return Integer.parseInt(getString(prop, key));
	}

	/**
	 * 读取 boolean 值
	 *
	 * <p>使用 {@link Boolean#valueOf(String)} 解析：忽略大小写等于 {@code "true"} 时返回 {@code true}，
	 * key 不存在或其他值均返回 {@code false}。
	 *
	 * @param prop 属性集合，不可为 {@code null}
	 * @param key  属性名，可为 {@code null}
	 * @return 解析后的 boolean 值，缺失或非法值返回 {@code false}
	 * @throws NullPointerException 当 {@code prop} 为 {@code null} 时抛出
	 */
	public static boolean getBoolean(Properties prop, String key) {
		return Boolean.valueOf(getString(prop, key));
	}

	/**
	 * 读取 long 值
	 *
	 * @param prop 属性集合，不可为 {@code null}
	 * @param key  属性名，可为 {@code null}
	 * @return 解析后的 long 值
	 * @throws NullPointerException     当 {@code prop} 为 {@code null} 时抛出
	 * @throws NumberFormatException 当 key 不存在（值为 {@code null}）或值不是合法长整数时抛出
	 */
	public static long getLong(Properties prop, String key) {
		return Long.valueOf(getString(prop, key));
	}

	/**
	 * 读取 double 值
	 *
	 * @param prop 属性集合，不可为 {@code null}
	 * @param key  属性名，可为 {@code null}
	 * @return 解析后的 double 值
	 * @throws NullPointerException     当 {@code prop} 为 {@code null} 时抛出
	 * @throws NumberFormatException 当 key 不存在（值为 {@code null}）或值不是合法浮点数时抛出
	 */
	public static double getDouble(Properties prop, String key) {
		return Double.valueOf(getString(prop, key));
	}


}
