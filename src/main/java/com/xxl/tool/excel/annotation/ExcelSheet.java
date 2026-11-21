package com.xxl.tool.excel.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.*;

/**
 * Excel Sheet 注解：声明 Java 类对应的 Sheet 名称与表头颜色。
 *
 * <p>作用于类；写入时每个类生成一个 Sheet，读取时按 {@link #name()} 或类
 * 简单名查找 Sheet。注解本身无副作用，POI 为可选依赖（provided）。
 *
 * @author xuxueli 2017-09-08 20:51:26
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelSheet {

    /**
     * Sheet 名称。
     *
     * <p>默认 {@code ""}，表示使用类简单名；非空白时去除首尾空白后使用。写入时
     * 若同名 Sheet 已存在，会依次尝试追加 {@code 2}、{@code 3}…（最多到
     * {@code 1000}）避免重名。
     *
     * @return Sheet 名称，默认空字符串（使用类简单名）
     */
    String name() default "";

    /**
     * 表头行背景色。
     *
     * <p>默认 {@link IndexedColors#LIGHT_GREEN}；仅写入时生效，使用
     * {@link IndexedColors#getIndex()} 作为填充色并设置为实心填充，读取时忽略。
     *
     * @return 表头背景色，默认 LIGHT_GREEN
     */
    IndexedColors headColor() default IndexedColors.LIGHT_GREEN;

}
