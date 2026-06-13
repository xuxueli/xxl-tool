package com.xxl.tool.http.http.auth.impl;

import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Header;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP Basic Authentication provider.
 *
 * <p>
 * Sets the {@code Authorization} header to {@code Basic base64(username:password)}.
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public class BasicAuthProvider implements AuthProvider {

    private final String username;
    private final String password;

    public BasicAuthProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void apply(HttpRequest request) {
        String encoded = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        request.header(Header.AUTHORIZATION.getValue(), "Basic " + encoded);
    }

}
