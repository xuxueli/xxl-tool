package com.xxl.tool.http.http.auth.impl;

import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.HttpRequest;

/**
 * API Key Authentication provider.
 *
 * <p>
 * Supports placing the API key in a request header or as a URL query parameter.
 * 同一实例可重复用于多个请求；构造后不可变，线程安全。QUERY 方式直接拼接 key/value，
 * 若值包含 {@code &}、{@code =}、空格等字符需调用方自行 URL 编码。
 * </p>
 *
 * @author xuxueli 2026-06-13
 */
public class ApiKeyAuthProvider implements AuthProvider {

    /**
     * Where to place the API key.
     */
    public enum Location {
        /** API key in a request header (e.g. {@code X-API-Key: xxx}). */
        HEADER,
        /** API key as a URL query parameter (e.g. {@code ?api_key=xxx}). */
        QUERY
    }

    private final String keyName;
    private final String keyValue;
    private final Location location;

    /**
     * 构造 API Key 认证提供者
     *
     * @param keyName  API Key 名称（请求头名或查询参数名），可为 {@code null}
     * @param keyValue API Key 值，可为 {@code null}（按字符串 {@code null} 拼接）
     * @param location 放置位置，{@code null} 时按 {@link Location#QUERY} 处理
     */
    public ApiKeyAuthProvider(String keyName, String keyValue, Location location) {
        this.keyName = keyName;
        this.keyValue = keyValue;
        this.location = location;
    }

    /**
     * 将 API Key 写入请求头或 URL 查询参数
     *
     * @param request 待认证请求，不能为 {@code null}；QUERY 方式下其 URL 不能为 {@code null}
     */
    @Override
    public void apply(HttpRequest request) {
        if (location == Location.HEADER) {
            request.header(keyName, keyValue);
        } else {
            String url = request.getUrl();
            String separator = url.contains("?") ? "&" : "?";
            request.url(url + separator + keyName + "=" + keyValue);
        }
    }

}
