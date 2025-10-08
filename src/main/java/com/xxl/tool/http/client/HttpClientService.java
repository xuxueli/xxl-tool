package com.xxl.tool.http.client;

import java.lang.annotation.*;

/**
 * http client service
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HttpClientService {

    /**
     * request base url
     */
    String url() default "";

    /**
     * request path for service, default ""
     */
    String path() default "";

    /**
     * request timeout, milliseconds
     */
    int timeout() default -1;

}