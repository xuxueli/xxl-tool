package com.xxl.tool.http.client;

import java.lang.annotation.*;

/**
 * http client method
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HttpClientMethod {

    /**
     * request path for method, default "method.getName()"
     */
    String path() default "";

    /**
     * request timeout, milliseconds
     */
    int timeout() default -1;

}