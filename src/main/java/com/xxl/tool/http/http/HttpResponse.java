package com.xxl.tool.http.http;

import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * HTTP 响应模型：状态码、响应体（字符串）、请求 URL 与响应 Cookie
 *
 * <p>
 * 通常由 {@link HttpRequest#execute()} 创建；响应体已由请求端按 UTF-8 读取，
 * {@link #response(Class, Type...)} 可进一步将 JSON 响应反序列化为对象。
 * 本类为可变 POJO，<b>非线程安全</b>，仅在单次请求的处理链路内使用。
 * </p>
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


    /**
     * 设置 Http 状态码
     *
     * @param statusCode Http 状态码，例如 200
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 设置响应体
     *
     * @param response 响应体字符串，可为 {@code null}
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * 设置请求 URL
     *
     * @param url 原始请求 URL，可为 {@code null}
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 设置响应 Cookie
     *
     * @param cookies 解析后的 Cookie Map（仅 name/value），可为 {@code null}
     */
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    // ----------- tool -----------

    /**
     * 请求是否成功
     *
     * @return 状态码等于 200 时返回 {@code true}，其他状态码（包括 2xx 的 201/204）返回 {@code false}
     */
    public boolean isSuccess() {
        return this.statusCode == 200;
    }


    /**
     * Http 状态码
     *
     * @return Http 状态码；未设置时默认为 0
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Http 请求响应数据
     *
     * @return 原始响应体字符串；无响应体时为 {@code null}
     */
    public String response() {
        return response;
    }

    /**
     * Http 请求响应结果
     *
     * <p>
     * 使用 JSON 反序列化响应体；响应体为空白或 {@code typeOfT} 为 {@code null} 时直接返回 {@code null}。
     * </p>
     *
     * @param typeOfT       结果类型
     * @param typeArguments 泛型类型，仅在结果类型为泛型容器（如 {@code Response<RespDTO>}）时传入
     * @return T            结果对象，通过json反序列化获取；无响应体或类型为空时返回 {@code null}
     * @throws RuntimeException 状态码不为 200，或响应体不是合法 JSON 时抛出
     */
    public <T> T response(Class<T> typeOfT, Type... typeArguments) {
        // valid status-code
        if (!isSuccess()) {
            throw new RuntimeException("Http Request fail, statusCode(" + statusCode + ") for url : " + url);
        }

        // valid response
        if (StringTool.isBlank(this.response) || typeOfT == null) {
            return null;
        }

        // response 2 typeOfT
        if (typeArguments != null && typeArguments.length > 0 && typeArguments[0] != null) {
            return GsonTool.fromJson(this.response, typeOfT, typeArguments);
        } else {
            return GsonTool.fromJson(this.response, typeOfT);
        }
    }

    /**
     * 请求 Url
     *
     * @return 原始请求 URL；未设置时为 {@code null}
     */
    public String url() {
        return url;
    }

    /**
     * Http 请求响应 Cookie
     *
     * @return 响应 Cookie Map（仅 name/value，忽略属性）；无 Cookie 时为 {@code null}
     */
    public Map<String, String> cookies() {
        return cookies;
    }

    /**
     * 获取指定 Cookie
     *
     * @param key Cookie 名称
     * @return Cookie 值；无 Cookie 或名称不存在时返回 {@code null}
     */
    public String cookie(String key) {
        return cookies != null ? cookies.get(key) : null;
    }

    /**
     * 输出响应摘要（包含响应体与 Cookie）
     *
     * @return 调试用字符串；可能包含敏感响应数据，不要直接返回给不可信调用方
     */
    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", response='" + response + '\'' +
                ", url='" + url + '\'' +
                ", cookies=" + cookies +
                '}';
    }

}