package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * properties tool
 *
 * @author xuxueli 2015-6-22 22:36:46
 */
public class PropTool {
	private static Logger logger = LoggerFactory.getLogger(PropTool.class);


	// ---------------------- load prop value ----------------------

	/**
	 * load properties from resource path
	 *
	 * @param resourcePath
	 * @return
	 */
	public static Properties loadProp(String resourcePath) {
		Properties prop = new Properties();
		if (resourcePath == null || resourcePath.trim().isEmpty()) {
			return prop;
		}

		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
			if (input != null) {
				prop.load(input);
			}
		} catch (Exception e) {
			logger.error("PropTool loadProp error:", e);
		}
		return prop;
	}

	/**
	 * load properties from file path
	 *
	 * @param fileName
	 * @return
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

		try (InputStream in = Files.newInputStream(path)) {
			prop.load(in);
		} catch (IOException e) {
			logger.error("PropTool loadFileProp error:", e);
		}
		return prop;
	}

	// ---------------------- read prop value ----------------------

	/**
	 * load string value
	 *
	 * @param key
	 * @return
	 */
	public static String getString(Properties prop, String key) {
		return prop.getProperty(key);
	}

	/**
	 * load string value with default value
	 *
	 * @param prop
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getString(Properties prop, String key, String defaultValue) {
		String value = getString(prop, key);
		if (value==null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * load int value
	 *
	 * @param key
	 * @return
	 */
	public static int getInt(Properties prop, String key) {
		return Integer.parseInt(getString(prop, key));
	}

	/**
	 * load int boolean
	 *
	 * @param prop
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(Properties prop, String key) {
		return Boolean.valueOf(getString(prop, key));
	}

	/**
	 * load long value
	 *
	 * @param prop
	 * @param key
	 * @return
	 */
	public static long getLong(Properties prop, String key) {
		return Long.valueOf(getString(prop, key));
	}

	/**
	 * load double value
	 *
	 * @param prop
	 * @param key
	 * @return
	 */
	public static double getDouble(Properties prop, String key) {
		return Double.valueOf(getString(prop, key));
	}


}
