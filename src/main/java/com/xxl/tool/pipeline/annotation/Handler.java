package com.xxl.tool.pipeline.annotation;

import java.lang.annotation.*;

/**
 * pipeline handler annotion
 *
 * @author xuxueli 2024-01-01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface  Handler {

    String value();

}