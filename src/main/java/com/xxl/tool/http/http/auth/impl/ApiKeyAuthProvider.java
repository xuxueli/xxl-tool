package com.xxl.tool.http.http.auth.impl;

import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.HttpRequest;

/**
 * API Key Authentication provider.
 *
 * <p>
 * Supports placing the API key in a request header or as a URL query parameter.
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

    public ApiKeyAuthProvider(String keyName, String keyValue, Location location) {
        this.keyName = keyName;
        this.keyValue = keyValue;
        this.location = location;
    }

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
