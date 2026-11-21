package com.xxl.tool.http.client;

import java.lang.annotation.*;

/**
 * http client service：声明代理接口的 base URL、路径前缀与默认超时
 *
 * <p>
 * 仅对通过 {@link HttpClient#proxy(Class)} 创建的代理接口生效；客户端显式设置的非空 URL
 * 优先于注解中的 {@link #url()}。
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HttpClientService {

    /**
     * request base url
     *
     * @return 接口级 base URL；空串表示使用客户端设置
     */
    String url() default "";

    /**
     * request path for service, default ""
     *
     * @return 路径前缀，自动去除首尾 {@code /}；空串表示 URL 中不追加前缀
     */
    String path() default "";

    /**
     * request timeout, milliseconds
     *
     * <p>
     * 同时作用于连接与读取超时；优先级低于方法级 {@link HttpClientMethod#timeout()}，
     * 高于客户端默认值。
     * </p>
     *
     * @return 超时时间，单位：毫秒；{@code -1}（或非正数）表示不覆盖客户端配置
     */
    int timeout() default -1;

}