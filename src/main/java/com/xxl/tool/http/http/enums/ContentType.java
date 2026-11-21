package com.xxl.tool.http.http.enums;

import java.nio.charset.Charset;

/**
 * Content Type：常用 HTTP 请求内容类型及其 charset 拼接
 *
 * <p>
 * 枚举不可变、线程安全；{@link #getValue(Charset)} 仅做字符串拼接，不校验 charset。
 * </p>
 */
public enum ContentType {
    /**
     * 标准表单编码，get请求时用x-www-form-urlencoded编码方式把form数据转换成一个字串（name1=value1&name2=value2…）
     */
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    /**
     * JSON编码
     */
    JSON("application/json"),
    /**
     * XML编码
     */
    XML("application/xml"),
    /**
     * text/plain编码
     */
    TEXT_PLAIN("text/plain"),
    /**
     * text/xml编码
     */
    TEXT_XML("text/xml"),
    /**
     * text/html编码
     */
    TEXT_HTML("text/html");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    /**
     * 获取 value
     *
     * @return Content-Type 字面值，例如 {@code application/json}
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取 value + charset
     *
     * @param charset 字符集，不能为 {@code null}
     * @return 形如 {@code application/json;charset=UTF-8} 的完整 Content-Type
     *
     * <pre>
     *     ContentType.JSON.getValue(StandardCharsets.UTF_8)   = application/json;charset=UTF-8
     *     ContentType.JSON.getValue()                         = application/json
     * </pre>
     */
    public String getValue(Charset charset) {
        return getValue() + ";charset=" + charset.name();
    }

    /**
     * 输出枚举摘要
     *
     * @return 包含 Content-Type 字面值的调试字符串
     */
    @Override
    public String toString() {
        return "ContentType{" +
                "value='" + value + '\'' +
                '}';
    }
}