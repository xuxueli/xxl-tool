package com.xxl.tool.excel.annotation;

import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.lang.annotation.*;

/**
 * Excel 字段注解：声明 Java 字段与 Excel 列（表头）的映射及写入样式。
 *
 * <p>作用于字段，读写时由 {@code ExcelTool} 解析；未标注该注解的非静态字段
 * 仍会参与读写，列名取字段名。静态字段始终忽略。
 *
 * <p>支持写入/读取的 Java 类型：{@code Boolean、String、Short、Integer、Long、
 * Float、Double、Date、Enum}。写入时统一以字符串单元格落盘（日期按
 * {@link #dateformat()} 格式化），读取时按字段类型转换；非法数值或日期会抛出
 * {@link RuntimeException}。
 *
 * <p>注解本身无副作用；POI 为可选依赖（provided，需使用方自行引入）。
 *
 * @author xuxueli 2017-09-08 20:22:41
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelField {

    /**
     * 列名（表头文本）。
     *
     * <p>默认 {@code ""}，表示使用 Java 字段名；非空白时去除首尾空白后作为
     * 写入表头与读取时的列匹配依据。
     *
     * @return 列名，默认空字符串（使用字段名）
     */
    String name() default "";

    /**
     * 列宽。
     *
     * <p>默认 {@code 0}，表示自动列宽（{@code Sheet#autoSizeColumn}）；大于 0
     * 时直接设置为 POI 列宽（单位为 1/256 个字符宽度），不再自动计算。
     *
     * @return 列宽，默认 0（自动）
     */
    int width() default 0;

    /**
     * 数据单元格的水平对齐方式，同时会克隆到表头样式。
     *
     * @return 对齐方式，默认 {@link HorizontalAlignment#LEFT}
     */
    HorizontalAlignment align() default HorizontalAlignment.LEFT;

    /**
     * 日期格式。
     *
     * <p>写入时用于格式化 {@link java.util.Date}，读取时用于解析日期文本；
     * 使用 {@link java.text.SimpleDateFormat} 语法，默认
     * {@code "yyyy-MM-dd HH:mm:ss"}。格式非法或文本不匹配时抛出运行时异常。
     *
     * @return 日期格式，默认 {@code "yyyy-MM-dd HH:mm:ss"}
     */
    String dateformat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 是否忽略该字段。
     *
     * <p>默认 {@code false}；为 {@code true} 时该字段既不写入 Excel，也不从
     * Excel 读取（跳过对应列）。
     *
     * @return 是否忽略，默认 {@code false}
     */
    boolean ignore() default false;

}
