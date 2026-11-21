package com.xxl.tool.pipeline.annotation;

import java.lang.annotation.*;

/**
 * Pipeline Handler 注解
 *
 * <p>用于在 Handler 类型上声明名称等元数据，运行期保留且可被继承。
 * 当前 {@code SpringPipelineExecutor} 按 Spring Bean 名称装配 Handler，并不读取本注解；
 * 本注解仅作为可选的声明式标记。</p>
 *
 * @author xuxueli 2024-01-01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface  Handler {

    /**
     * Handler 名称
     *
     * @return Handler 名称，作为声明式标识
     */
    String value();

}