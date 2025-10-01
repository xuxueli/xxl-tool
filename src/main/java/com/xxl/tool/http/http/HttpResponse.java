package com.xxl.tool.http.http;

import com.xxl.tool.core.StringTool;
import com.xxl.tool.gson.GsonTool;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Http Response
 */
public class HttpResponse {

    /**
     * Http 状态码
     * - 200 成功
     * - 404 失败
     */
    private int statusCode;

    /**
     * Http 请求响应数据
     */
    private String response;

    /**
     * 请求 Url (From HttpRequest)
     */
    private String url;

    /**
     * Http 请求响应 Cookie
     */
    private Map<String, String> cookies;


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    // ----------- tool -----------

    /**
     * 请求是否成功
     */
    public boolean isSuccess() {
        return this.statusCode == 200;
    }


    /**
     * Http 状态码
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Http 请求响应数据
     */
    public String response() {
        return response;
    }

    /**
     * Http 请求响应结果
     *
     * @param typeOfT       结果类型
     * @param typeArguments 泛型类型
     * @return T            结果对象，通过json反序列化获取
     */
    public <T> T response(Class<T> typeOfT, Type... typeArguments) {
        if (StringTool.isBlank(this.response) || typeOfT == null) {
            return null;
        }
        if (!isSuccess()) {
            throw new RuntimeException("Http Request fail, statusCode(" + statusCode + ") for url : " + url);
        }

        // result 2 reponse
        if (typeArguments != null && typeArguments.length > 0 && typeArguments[0] != null) {
            return GsonTool.fromJson(this.response, typeOfT, typeArguments);
        } else {
            return GsonTool.fromJson(this.response, typeOfT);
        }
    }

    /**
     * 请求 Url
     */
    public String url() {
        return url;
    }

    /**
     * Http 请求响应 Cookie
     */
    public Map<String, String> cookies() {
        return cookies;
    }

    /**
     * 获取指定 Cookie
     */
    public String cookie(String key) {
        return cookies != null ? cookies.get(key) : null;
    }

}