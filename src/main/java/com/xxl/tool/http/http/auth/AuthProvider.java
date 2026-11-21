package com.xxl.tool.http.http.auth;

import com.xxl.tool.http.http.HttpRequest;

/**
 * Authentication provider for HTTP requests.
 *
 * <p>
 * Implementations apply authentication (headers, URL params, etc.) to an HttpRequest.
 * 实现会在建立连接前被 {@link HttpRequest} 调用，可直接修改请求头或 URL；实现应保持无状态、
 * 线程安全，不要在异常或日志中输出凭证。
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public interface AuthProvider {

    /**
     * Apply authentication to the given HTTP request.
     *
     * @param request  the HTTP request to authenticate，不能为 {@code null}；方法内直接修改其头部或 URL
     */
    void apply(HttpRequest request);

}
