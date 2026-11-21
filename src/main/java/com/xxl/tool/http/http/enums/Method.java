package com.xxl.tool.http.http.enums;

/**
 * HTTP Method：支持的标准请求方法
 *
 * <p>
 * 枚举不可变、线程安全；{@link #toString()} 即方法名，直接用于 {@code HttpURLConnection#setRequestMethod}。
 * 部分 JVM 实现不支持 PATCH，本枚举未包含。
 * </p>
 */
public enum Method {

    /** GET：获取资源，参数拼接到 URL，不发送请求体 */
    GET,
    /** POST：提交数据，请求体可为 JSON 或表单 */
    POST,
    /** HEAD：仅获取响应头，无响应体 */
    HEAD,
    /** OPTIONS：查询资源支持的方法 */
    OPTIONS,
    /** PUT：整体更新资源 */
    PUT,
    /** DELETE：删除资源 */
    DELETE,
    /** TRACE：回显请求，主要用于诊断 */
    TRACE;
}