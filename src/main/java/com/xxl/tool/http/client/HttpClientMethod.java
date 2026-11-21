package com.xxl.tool.http.client;

import java.lang.annotation.*;

/**
 * http client method：声明代理接口单个方法的请求路径与超时
 *
 * <p>
 * 仅对通过 {@link HttpClient#proxy(Class)} 创建的代理接口生效；未标注时使用方法名作为路径，
 * 超时继承接口/客户端配置。
 * </p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HttpClientMethod {

    /**
     * request path for method, default "method.getName()"
     *
     * @return 方法级路径片段，自动去除首尾 {@code /}；空串表示使用方法名
     */
    String path() default "";

    /**
     * request timeout, milliseconds
     *
     * <p>
     * 同时作用于连接与读取超时，优先级高于 {@link HttpClientService#timeout()} 与客户端默认值。
     * </p>
     *
     * @return 超时时间，单位：毫秒；{@code -1}（或非正数）表示不覆盖上层配置
     */
    int timeout() default -1;

}