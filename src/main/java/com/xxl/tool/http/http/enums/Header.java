package com.xxl.tool.http.http.enums;

public enum Header {

    /**
     * 控制当前连接的行为，决定是否复用连接或在请求完成后关闭连接。
     * 可选值：
     * - Keep-Alive：保持连接打开，允许后续请求复用该连接；减少连接建立开销，适用于需要发送多个请求的场景；（推荐）
     * - close：请求完成后立即关闭连接；适用于单次请求或资源密集型操作；
     */
    CONNECTION("Connection"),

    /**
     * 资源的 MIME 类型，告诉服务器实际发送的数据类型。
     * 可选值：
     * -  application/x-www-form-urlencoded：表单数据（默认编码方式）
     * -  application/json：JSON 数据
     * -  application/xml：XML 数据
     * -  text/html：HTML 文档
     * -  text/plain：纯文本
     */
    CONTENT_TYPE("Content-Type"),

    /**
     * 告诉服务器客户端可以接受的字符编码集
     * 可选值：
     * - UTF-8：最常用的字符编码，支持所有语言
     * - GBK：主要用于中文编码
     */
    ACCEPT_CHARSET("Accept-Charset"),

    /**
     * 请求头中包含的Cookie信息
     * 格式：
     * - name1=value1; name2=value2; name3=value3
     */
    COOKIE("Cookie"),

    /**
     * HTTP 身份验证机制
     * 可选方式：
     * - Basic Auth：基本身份验证，使用用户名和密码进行加密传输
     * - Bearer Token：通常用于 OAuth2 认证，Token 放在请求头的 Authorization 字段中，格式为 Bearer <token>
     * - Digest Auth：摘要身份验证，一种更安全的认证方式，它通过 MD5 哈希算法对用户名、密码和请求数据进行加密
     * - API Key: API 密钥认证，通过在请求头中包含一个特殊的 API Key 来标识请求的合法性
     *
     * <pre>
     *     1、Basic格式：
     *          Authorization: Basic base64( "{username}:{password}" )
     *     2、Bearer格式：
     *          Authorization: Bearer token
     *     3、Digest格式：
     *          Authorization: Digest username="Mufasa", realm="……", ……
     *     4、API Key格式：
     *          X-API-Key: xxxxx
     * </pre>
     */
    AUTHORIZATION("Authorization"),

    /**
     * 服务器返回给客户端的一个或多个 Cookie 信息
     * 格式：
     * - name1=value1; name2=value2; name3=value3
     */
    SET_COOKIE("Set-Cookie");

    private final String value;

    Header(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "Header{" +
                "value='" + value + '\'' +
                '}';
    }

}