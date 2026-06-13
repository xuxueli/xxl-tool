package com.xxl.tool.http.http.auth;

import com.xxl.tool.http.http.HttpRequest;

/**
 * Authentication provider for HTTP requests.
 *
 * <p>
 * Implementations apply authentication (headers, URL params, etc.) to an HttpRequest.
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public interface AuthProvider {

    /**
     * Apply authentication to the given HTTP request.
     *
     * @param request  the HTTP request to authenticate
     */
    void apply(HttpRequest request);

}
