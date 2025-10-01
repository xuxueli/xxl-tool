package com.xxl.tool.http.http.iface;

import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.HttpResponse;

public interface HttpInterceptor {

    /**
     * before execute
     */
    void before(HttpRequest httpRequest);

    /**
     * after execute
     */
    void after(HttpRequest httpRequest, HttpResponse httpResponse);

}