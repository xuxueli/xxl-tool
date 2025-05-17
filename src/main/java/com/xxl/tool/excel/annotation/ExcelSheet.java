package com.xxl.tool.excel.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.*;

/**
 * excel sheet info
 *
 * @author xuxueli 2017-09-08 20:51:26
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelSheet {

    /**
     * sheet 名称
     *
     * @return String
     */
    String name() default "";

    /**
     * sheet 首行颜色
     *
     * @return IndexedColors
     */
    IndexedColors headColor() default IndexedColors.LIGHT_GREEN;

}
