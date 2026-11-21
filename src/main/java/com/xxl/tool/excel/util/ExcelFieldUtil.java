package com.xxl.tool.excel.util;

import com.xxl.tool.excel.annotation.ExcelField;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Excel 字段值转换工具：在 Excel 单元格字符串与 Java 字段类型之间转换。
 *
 * <p>供 {@code ExcelTool} 内部使用，也可单独调用。支持 {@code Boolean、String、
 * Short、Integer、Long、Float、Double、Date、Enum}；{@code parseValue} 会先去除
 * 首尾空白，各 {@code parseX} 方法会移除全角空格；空值返回 {@code null}，非法
 * 输入抛出 {@link RuntimeException}。
 *
 * <p>无状态工具类（构造器私有），方法线程安全；不使用 POI 的单元格类型，仅读取
 * {@code @ExcelField} 注解上的日期格式。
 *
 * @author xuxueli 2017-05-26
 */
public final class ExcelFieldUtil {

	private ExcelFieldUtil(){}

	/**
	 * 解析 Byte（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Byte 文本
	 */
	public static Byte parseByte(String value) {
		try {
			value = value.replaceAll("　", "");
			return Byte.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseByte but input illegal input=" + value, e);
		}
	}

	/**
	 * 解析 Boolean（忽略大小写的 {@code true}/{@code false}，会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是 {@code true}/{@code false}
	 */
	public static Boolean parseBoolean(String value) {
		value = value.replaceAll("　", "");
		if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
			return Boolean.TRUE;
		} else if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
			return Boolean.FALSE;
		} else {
			throw new RuntimeException("parseBoolean but input illegal input=" + value);
		}
	}

	/**
	 * 解析 Integer（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Integer 文本
	 */
	public static Integer parseInt(String value) {
		try {
			value = value.replaceAll("　", "");
			return Integer.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseInt but input illegal input=" + value, e);
		}
	}

	/**
	 * 解析 Short（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Short 文本
	 */
	public static Short parseShort(String value) {
		try {
			value = value.replaceAll("　", "");
			return Short.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseShort but input illegal input=" + value, e);
		}
	}

	/**
	 * 解析 Long（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Long 文本
	 */
	public static Long parseLong(String value) {
		try {
			value = value.replaceAll("　", "");
			return Long.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseLong but input illegal input=" + value, e);
		}
	}

	/**
	 * 解析 Float（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Float 文本
	 */
	public static Float parseFloat(String value) {
		try {
			value = value.replaceAll("　", "");
			return Float.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseFloat but input illegal input=" + value, e);
		}
	}

	/**
	 * 解析 Double（会先移除全角空格）。
	 *
	 * @param value 待解析字符串
	 * @return 解析结果
	 * @throws NullPointerException 参数为 {@code null}
	 * @throws RuntimeException 不是合法的 Double 文本
	 */
	public static Double parseDouble(String value) {
		try {
			value = value.replaceAll("　", "");
			return Double.valueOf(value);
		} catch(NumberFormatException e) {
			throw new RuntimeException("parseDouble but input illegal input=" + value, e);
		}
	}

	/**
	 * 按 {@link ExcelField#dateformat()} 解析日期。
	 *
	 * <p>注解为 {@code null} 时使用默认格式 {@code "yyyy-MM-dd HH:mm:ss"}；
	 * 使用 {@link SimpleDateFormat} 的默认宽松模式解析。
	 *
	 * @param value 日期文本
	 * @param excelField 字段注解，可为 {@code null}（使用默认格式）
	 * @return 解析后的日期
	 * @throws NullPointerException {@code value} 为 {@code null}
	 * @throws RuntimeException 文本与日期格式不匹配
	 */
	public static Date parseDate(String value, ExcelField excelField) {
		try {
			String datePattern = "yyyy-MM-dd HH:mm:ss";
			if (excelField != null) {
				datePattern = excelField.dateformat();
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
			return dateFormat.parse(value);
		} catch(ParseException e) {
			throw new RuntimeException("parseDate but input illegal input=" + value, e);
		}
	}

	/**
	 * 按字段声明类型将单元格字符串解析为 Java 对象。
	 *
	 * <p>空白字符串（{@code null}、空串或仅含空白）返回 {@code null}；否则去除
	 * 首尾空白后按字段类型转换。日期格式取字段上
	 * {@code @ExcelField.dateformat()}（无注解时默认 {@code "yyyy-MM-dd HH:mm:ss"}），
	 * 枚举按 {@link Enum#valueOf(Class, String)} 匹配名称。
	 * 支持的类型：Boolean、String、Short、Integer、Long、Float、Double、Date、Enum。
	 *
	 * @param field 目标字段，不能为 {@code null}；仅使用其类型与注解
	 * @param value 单元格文本，可为 {@code null}
	 * @return 解析后的对象；空白文本返回 {@code null}
	 * @throws RuntimeException 文本与字段类型不匹配，或字段类型不受支持
	 */
	public static Object parseValue(Field field, String value) {
		Class<?> fieldType = field.getType();

		ExcelField excelField = field.getAnnotation(ExcelField.class);
		if(value==null || value.trim().isEmpty()) {
			return null;
		}
		value = value.trim();

		/*if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
			return parseByte(value);
		} else */
		/* else if (Character.class.equals(fieldType) || Character.TYPE.equals(fieldType)) {
			 return value.toCharArray()[0];
		}*/
		if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
			return parseBoolean(value);
		} else if (String.class.equals(fieldType)) {
			return value;
		} else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
			return parseShort(value);
		} else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
			return parseInt(value);
		} else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
			return parseLong(value);
		} else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
			return parseFloat(value);
		} else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
			return parseDouble(value);
		} else if (Date.class.equals(fieldType)) {
			return parseDate(value, excelField);
		} else if (fieldType.isEnum()) {
			return Enum.valueOf((Class<Enum>)fieldType, value);
		} else {
			throw new RuntimeException("request illeagal type, type must be Integer not int Long not long etc, type=" + fieldType);
		}
	}

	/**
	 * 将字段值格式化为写入 Excel 的字符串。
	 *
	 * <p>{@code null} 返回 {@code null}；{@code Date} 按字段上
	 * {@code @ExcelField.dateformat()}（无注解时默认 {@code "yyyy-MM-dd HH:mm:ss"}）
	 * 格式化，枚举取 {@link Enum#name()}，其余支持类型使用
	 * {@link String#valueOf(Object)}。写入时统一作为字符串单元格。
	 *
	 * @param field 源字段，不能为 {@code null}；仅使用其类型与注解
	 * @param value 字段值，可为 {@code null}
	 * @return 格式化后的字符串；值为 {@code null} 时返回 {@code null}
	 * @throws RuntimeException 字段类型不受支持
	 */
	public static String formatValue(Field field, Object value) {
		Class<?> fieldType = field.getType();

		ExcelField excelField = field.getAnnotation(ExcelField.class);
		if(value==null) {
			return null;
		}

		if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (String.class.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
			return String.valueOf(value);
		} else if (Date.class.equals(fieldType)) {
			String datePattern = "yyyy-MM-dd HH:mm:ss";
			if (excelField != null && excelField.dateformat()!=null) {
				datePattern = excelField.dateformat();
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
			return dateFormat.format(value);
		} else if (fieldType.isEnum()) {
			return ((Enum<?>)value).name();
		} else {
			throw new RuntimeException("request illeagal type, type must be Integer not int Long not long etc, type=" + fieldType);
		}
	}

}