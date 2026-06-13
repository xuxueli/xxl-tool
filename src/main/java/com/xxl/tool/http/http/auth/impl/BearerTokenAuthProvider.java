package com.xxl.tool.http.http.auth.impl;

import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Header;

/**
 * Bearer Token Authentication provider.
 *
 * <p>
 * Sets the {@code Authorization} header to {@code Bearer <token>}.
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public class BearerTokenAuthProvider implements AuthProvider {

    private final String token;

    public BearerTokenAuthProvider(String token) {
        this.token = token;
    }

    @Override
    public void apply(HttpRequest request) {
        request.header(Header.AUTHORIZATION.getValue(), "Bearer " + token);
    }

}
