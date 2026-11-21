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
 * 使用 UTF-8 编码用户名/密码；构造后不可变，线程安全。注意 Basic 认证仅做 Base64 编码，
 * 必须配合 HTTPS 使用，凭证不得写入日志。
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public class BasicAuthProvider implements AuthProvider {

    private final String username;
    private final String password;

    /**
     * 构造 Basic 认证提供者
     *
     * @param username 用户名，可为 {@code null}
     * @param password 密码，可为 {@code null}
     */
    public BasicAuthProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 写入 {@code Authorization: Basic ...} 请求头
     *
     * @param request 待认证请求，不能为 {@code null}
     */
    @Override
    public void apply(HttpRequest request) {
        String encoded = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        request.header(Header.AUTHORIZATION.getValue(), "Basic " + encoded);
    }

}
