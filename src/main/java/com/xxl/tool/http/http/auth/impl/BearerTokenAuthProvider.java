package com.xxl.tool.http.http.auth.impl;

import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Header;

/**
 * Bearer Token Authentication provider.
 *
 * <p>
 * Sets the {@code Authorization} header to {@code Bearer <token>}.
 * 构造后不可变，线程安全；Token 不得写入日志或异常消息。
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public class BearerTokenAuthProvider implements AuthProvider {

    private final String token;

    /**
     * 构造 Bearer Token 认证提供者
     *
     * @param token 访问令牌，可为 {@code null}
     */
    public BearerTokenAuthProvider(String token) {
        this.token = token;
    }

    /**
     * 写入 {@code Authorization: Bearer <token>} 请求头
     *
     * @param request 待认证请求，不能为 {@code null}
     */
    @Override
    public void apply(HttpRequest request) {
        request.header(Header.AUTHORIZATION.getValue(), "Bearer " + token);
    }

}
