package com.xxl.tool.excel.annotation;

import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.lang.annotation.*;

/**
 * sheet cell info
 *
 *      支持Java对象数据类型：Boolean、String、Short、Integer、Long、Float、Double、Date
 *      支持Excel的Cell类型为：String
 *
 * @author xuxueli 2017-09-08 20:22:41
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelField {

    /**
     * cell name (defult use field-name)
     *
     * @return String
     */
    String name() default "";

    /**
     * cell width (effect when great than 0; default auto width；)
     *
     * @return int
     */
    int width() default 0;

    /**
     * cell align
     *
     * @return HorizontalAlignment
     */
    HorizontalAlignment align() default HorizontalAlignment.LEFT;

    /**
     * cell value format, for Date
     *
     * @return String
     */
    String dateformat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * ignore when excel write and read (default false)
     *
     * @return
     */
    boolean ignore() default false;

}
