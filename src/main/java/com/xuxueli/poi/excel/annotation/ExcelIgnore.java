package com.xuxueli.poi.excel.annotation;

import java.lang.annotation.*;

/**
 * @Author: kba977
 * @Date: 2017/12/21 下午6:08
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelIgnore {
}
