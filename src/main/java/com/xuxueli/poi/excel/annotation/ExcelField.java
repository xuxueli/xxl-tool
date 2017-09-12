package com.xuxueli.poi.excel.annotation;

import java.lang.annotation.*;

/**
 * 列属性信息
 *
 * @author xuxueli 2017-09-08 20:22:41
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelField {

    /**
     * 列名称
     *
     * @return
     */
    String name() default "";

}
