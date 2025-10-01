package com.xxl.tool.http;

import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xuxueli 2018-11-25 00:55:31
 */
public class HttpTool {
    private static final Logger logger = LoggerFactory.getLogger(HttpTool.class);

    // ---------------------- build ----------------------

    /**
     * build HttpRequest
     *
     * @return HttpRequest
     */
    public static HttpRequest createRequest() {
        return new HttpRequest();
    }

    /**
     * build HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createRequest(String url) {
        return new HttpRequest().url(url);
    }

    /**
     * build GET HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createGet(String url) {
        return new HttpRequest().url(url).method(Method.GET);
    }

    /**
     * build POST HttpRequest
     *
     * @param url request url
     * @return HttpRequest
     */
    public static HttpRequest createPost(String url) {
        return new HttpRequest().url(url).method(Method.POST);
    }

    // ---------------------- tool ----------------------

    /**
     * 检测是否https
     */
    public static boolean isHttps(String url) {
        return url != null && url.toLowerCase().startsWith("https:");
    }

    /**
     * 检测是否http
     */
    public static boolean isHttp(String url) {
        return url != null && url.toLowerCase().startsWith("http:");
    }

    // ---------------------- request ----------------------


    // ---------------------- response ----------------------


}