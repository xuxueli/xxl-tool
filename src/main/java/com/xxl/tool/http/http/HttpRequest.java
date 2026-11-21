package com.xxl.tool.http.http;

import com.xxl.tool.core.AssertTool;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.auth.impl.ApiKeyAuthProvider;
import com.xxl.tool.http.http.auth.impl.BasicAuthProvider;
import com.xxl.tool.http.http.auth.impl.BearerTokenAuthProvider;
import com.xxl.tool.http.http.enums.ContentType;
import com.xxl.tool.http.http.enums.Header;
import com.xxl.tool.http.http.enums.Method;
import com.xxl.tool.http.http.iface.HttpInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.tool.io.FileTool;
import com.xxl.tool.io.IOTool;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.xxl.tool.http.HttpTool.isHttps;

/**
 * HTTP 请求构建器与执行器：基于 JDK {@link HttpURLConnection} 实现
 *
 * <p>
 * 通过链式方法配置 URL、方法、Content-Type、请求头、Cookie、超时（单位均为毫秒）、
 * 请求体/表单、认证与拦截器，然后调用 {@link #execute()} 或 {@link #download(String)}。
 * 默认方法为 POST、Content-Type 为 JSON、连接与读取超时均为 3000ms、不使用缓存。
 * </p>
 *
 * <p>
 * 执行语义：每次 {@code execute}/{@code download} 都会新建连接并在结束后断开；
 * 响应体按 UTF-8 一次性读入内存（大文件请使用 {@code download}），响应流由本类关闭，调用方无需处理。
 * 重定向、代理沿用 JDK 默认行为（默认跟随重定向、使用 JVM 代理配置），本类未提供开关。
 * </p>
 *
 * <p>
 * <b>安全提示：</b>HTTPS 请求默认信任所有证书并跳过主机名校验（见 {@code trustAllHosts}），
 * 存在中间人攻击风险，不应用于不可信网络下的敏感通信；本类不会将认证头与 Cookie 写入日志。
 * </p>
 *
 * <p>
 * 本类为可变对象，<b>非线程安全</b>，不要在多个线程间共享同一个实例；一次请求使用一个实例。
 * </p>
 */
public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    // --------------------------------- field ---------------------------------

    private String url;                                     // 请求 Url
    private Method method = Method.POST;                    // Method
    private ContentType contentType = ContentType.JSON;     // Content-Type
    private Map<String, String> headers;                    // 存储请求头
    private Map<String, String> cookies;                    // Cookie（需要格式转换）
    private int connectTimeout = 3 * 1000;                  // 连接超时时间，单位：ms
    private int readTimeout = 3 * 1000;                     // 读取超时时间，单位：ms
    private boolean useCaches = false;                      // 是否使用缓存
    private String body;                                    // 存储请求体
    private Map<String, String> form;                       // 存储表单数据
    private AuthProvider authProvider;                      // 鉴权提供者
    private List<HttpInterceptor> interceptors;             // HttpInterceptor


    // --------------------------------- build ---------------------------------

    /**
     * 设置URL
     *
     * @param url 完整请求地址（http/https），允许 {@code null}，但执行时为空会抛异常
     * @return this
     */
    public HttpRequest url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置Method
     *
     * @param method 请求方法，允许 {@code null}，但执行时为空会抛异常
     * @return this
     */
    public HttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    /**
     * 设置Content-Type
     *
     * @param contentType 请求内容类型，允许 {@code null}，但执行时为空会抛异常
     * @return this
     */
    public HttpRequest contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置请求头，覆盖更新
     *
     * <p>
     * 先清空已有请求头再逐项写入；入参为 {@code null} 或空 Map 时不修改现有请求头。
     * </p>
     *
     * @param header 请求头 Map，可为 {@code null}
     * @return this
     */
    public HttpRequest header(Map<String, String> header) {
        // valid
        if (MapTool.isEmpty(header)) {
            return this;
        }

        if (MapTool.isNotEmpty(headers)) {
            headers.clear();
        }
        for (String key : header.keySet()) {
            header(key, header.get(key));
        }
        return this;
    }

    /**
     * 设置请求头
     *
     * <p>
     * 同名 key 覆盖旧值；key 为空白或 value 为 {@code null} 时忽略本次调用。
     * </p>
     *
     * @param key   请求头名称，为空白时忽略
     * @param value 请求头值，为 {@code null} 时忽略
     * @return this
     */
    public HttpRequest header(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.headers) {
            this.headers = new HashMap<>();
        }

        // set
        this.headers.put(key, value);
        return this;
    }

    /**
     * 设置请求头, 默认值
     *
     * <p>
     * 写入内置的桌面浏览器 User-Agent（{@link Header#DEFAULT_USER_AGENT_WIN}），
     * 已存在同名头时覆盖。
     * </p>
     *
     * @return this
     */
    public HttpRequest headerDefault() {
        header(Header.USER_AGENT.getValue(), Header.DEFAULT_USER_AGENT_WIN);
        return this;
    }

    /**
     * 设置Cookie，覆盖更新
     *
     * <p>
     * 先清空已有 Cookie 再逐项写入；入参为 {@code null} 或空 Map 时不修改现有 Cookie。
     * 发送时会拼接为 {@code name1=value1; name2=value2} 形式的 Cookie 请求头。
     * </p>
     *
     * @param cookie Cookie Map，可为 {@code null}
     * @return this
     */
    public HttpRequest cookie(Map<String, String> cookie) {
        // valid
        if (MapTool.isEmpty(cookie)) {
            return this;
        }

        if (MapTool.isNotEmpty(this.cookies)) {
            this.cookies.clear();
        }
        for (String key : cookie.keySet()) {
            cookie(key, cookie.get(key));
        }
        return this;
    }

    /**
     * 设置Cookie
     *
     * <p>
     * 同名 key 覆盖旧值；key 为空白或 value 为 {@code null} 时忽略本次调用。
     * value 原样发送，不做 URL 编码。
     * </p>
     *
     * @param key   Cookie 名称，为空白时忽略
     * @param value Cookie 值，为 {@code null} 时忽略
     * @return this
     */
    public HttpRequest cookie(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.cookies) {
            this.cookies = new HashMap<>();
        }

        // set
        this.cookies.put(key, value);
        return this;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间，单位：毫秒；0 表示不限制，负数在执行时抛 {@link IllegalArgumentException}
     * @return this
     */
    public HttpRequest connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 读取超时时间，单位：毫秒；0 表示不限制，负数在执行时抛 {@link IllegalArgumentException}
     * @return this
     */
    public HttpRequest readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 设置是否使用缓存
     *
     * @param useCaches 是否允许 {@link HttpURLConnection} 使用缓存，默认 {@code false}
     * @return this
     */
    public HttpRequest useCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    /**
     * 设置请求体 body
     *
     * <p>
     * 原样作为请求体发送（按 UTF-8 编码），优先级高于 {@code form}；GET 请求不会发送 body。
     * </p>
     *
     * @param body 请求体字符串，可为 {@code null}
     * @return this
     */
    public HttpRequest body(String body) {
        this.body = body;
        return this;
    }

    /**
     * 设置请求体 request
     *
     * @param request 请求体对象，将会序列化为 json (body 数据)；为 {@code null} 时不修改现有 body
     * @return this
     */
    public HttpRequest request(Object request) {
        if (Objects.nonNull(request)) {
            this.body = GsonTool.toJson(request);
        }
        return this;
    }

    /**
     * 设置表单 form
     *
     * <p>
     * GET 请求时表单会拼接到 URL 查询串；其他方法在未设置 body 时作为请求体发送，
     * 编码形式为 {@code name1=value1&name2=value2}（UTF-8）。注意本方法不会自动修改 Content-Type，
     * 默认仍为 JSON，需要时应显式调用 {@code contentType(ContentType.FORM_URLENCODED)}。
     * </p>
     *
     * @param key   表单字段名，为空白时忽略
     * @param value 表单字段值，为 {@code null} 时忽略
     * @return this
     */
    public HttpRequest form(String key, String value) {
        // valid
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }

        // init
        if (null == this.form) {
            this.form = new HashMap<>();
        }

        // set form
        this.form.put(key, value);
        return this;
    }

    /**
     * 设置表单 form
     *
     * <p>
     * 列表值以英文逗号连接后写入单个字段；列表为 {@code null} 或空时忽略本次调用。
     * </p>
     *
     * @param key   表单字段名
     * @param value 表单字段值列表，为 {@code null} 或空时忽略
     * @return this
     */
    public HttpRequest form(String key, List<String> value) {
        if (CollectionTool.isEmpty(value)) {
            return this;
        }
        return form(key, StringTool.join(value, ","));
    }

    /**
     * 设置表单 form，覆盖更新
     *
     * <p>
     * 先清空已有表单再逐项写入；入参为 {@code null} 或空 Map 时不修改现有表单。
     * </p>
     *
     * @param form 表单 Map，可为 {@code null}
     * @return this
     */
    public HttpRequest form(Map<String, String> form) {
        if (MapTool.isEmpty(form)) {
            return this;
        }

        if (MapTool.isNotEmpty(this.form)) {
            this.form.clear();
        }
        for (String key : form.keySet()) {
            form(key, form.get(key));
        }
        return this;
    }

    /**
     * 设置鉴权提供者
     *
     * <p>
     * 认证在建立连接前应用，可修改请求头或 URL；传入 {@code null} 表示不认证。
     * 后设置的认证方式会覆盖之前的设置。
     * </p>
     *
     * @param authProvider 鉴权提供者，可为 {@code null}
     * @return this
     */
    public HttpRequest auth(AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    /**
     * 设置 Basic Auth
     *
     * <p>
     * 生成 {@code Authorization: Basic base64(username:password)} 请求头，密码不会被日志输出。
     * </p>
     *
     * @param username 用户名，可为 {@code null}（按字符串 {@code null} 参与编码）
     * @param password 密码，可为 {@code null}
     * @return this
     */
    public HttpRequest basicAuth(String username, String password) {
        this.authProvider = new BasicAuthProvider(username, password);
        return this;
    }

    /**
     * 设置 Bearer Token Auth
     *
     * <p>
     * 生成 {@code Authorization: Bearer <token>} 请求头；Token 不会被日志输出。
     * </p>
     *
     * @param token 访问令牌，可为 {@code null}
     * @return this
     */
    public HttpRequest bearerAuth(String token) {
        this.authProvider = new BearerTokenAuthProvider(token);
        return this;
    }

    /**
     * 设置 API Key Auth
     *
     * <p>
     * 按 {@code location} 将 API Key 放入请求头或 URL 查询参数；QUERY 方式直接拼接，不做 URL 编码。
     * </p>
     *
     * @param keyName  API Key 名称（头名或参数名）
     * @param keyValue API Key 值
     * @param location 放置位置，{@code null} 时按 QUERY 处理
     * @return this
     */
    public HttpRequest apiKeyAuth(String keyName, String keyValue, ApiKeyAuthProvider.Location location) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, location);
        return this;
    }

    /**
     * 默认设置 API Key Auth, 默认位置为 HEADER
     *
     * @param keyName  API Key 请求头名称
     * @param keyValue API Key 值
     * @return this
     */
    public HttpRequest apiKeyAuth(String keyName, String keyValue) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, ApiKeyAuthProvider.Location.HEADER);
        return this;
    }

    /**
     * 设置拦截器，覆盖更新
     *
     * <p>
     * 先清空已有拦截器再按列表顺序添加；入参为 {@code null} 或空列表时不修改现有拦截器。
     * 执行时 {@code before} 与 {@code after} 均按添加顺序调用。
     * </p>
     *
     * @param interceptors 拦截器列表，可为 {@code null}
     * @return this
     */
    public HttpRequest interceptor(List<HttpInterceptor> interceptors) {
        if (CollectionTool.isEmpty(interceptors)) {
            return this;
        }

        if (CollectionTool.isNotEmpty(this.interceptors)) {
            this.interceptors.clear();
        }
        for (HttpInterceptor interceptor : interceptors) {
            interceptor(interceptor);
        }
        return this;
    }

    /**
     * 设置拦截器
     *
     * <p>
     * 追加到拦截器列表末尾；传入 {@code null} 时忽略。
     * </p>
     *
     * @param interceptor 拦截器，可为 {@code null}
     * @return this
     */
    public HttpRequest interceptor(HttpInterceptor interceptor) {
        if (Objects.isNull(interceptor)) {
            return this;
        }

        if (null == interceptors) {
            this.interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        return this;
    }


    // --------------------------------- get ---------------------------------

    /**
     * 获取格式化后的 Cookie: name1=value1; name2=value2; name3=value3
     * （Cookie 格式是一个键值对字符串，多个键值对之间用分号和空格分隔）
     *
     * @return Cookie 请求头值；未设置 Cookie 时返回 {@code null}
     */
    public String getCookieString() {
        if (MapTool.isEmpty(cookies)) {
            return null;
        }
        StringBuilder cookieHeader = new StringBuilder();
        for (String key : cookies.keySet()) {
            if (!cookieHeader.isEmpty() && !cookieHeader.toString().endsWith("; ")) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(key).append("=").append(cookies.get(key));
        }

        return cookieHeader.toString();
    }

    /**
     * 获取请求 URL
     *
     * @return 请求 URL；未设置时为 {@code null}
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取请求方法
     *
     * @return 请求方法，默认 POST
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取 Content-Type
     *
     * @return 内容类型，默认 JSON
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * 获取请求头
     *
     * @return 内部请求头 Map，可能为 {@code null}；返回的是可变引用，修改会影响本次请求
     */
    public Map<String, String> getHeader() {
        return headers;
    }

    /**
     * 获取 Cookie
     *
     * @return 内部 Cookie Map，可能为 {@code null}；返回的是可变引用
     */
    public Map<String, String> getCookie() {
        return cookies;
    }

    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间，单位：毫秒
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 获取读取超时时间
     *
     * @return 读取超时时间，单位：毫秒
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * 是否使用缓存
     *
     * @return 允许使用缓存返回 {@code true}
     */
    public boolean isUseCaches() {
        return useCaches;
    }

    /**
     * 获取请求体
     *
     * @return 请求体字符串；未设置时为 {@code null}
     */
    public String getBody() {
        return body;
    }

    /**
     * 获取表单数据
     *
     * @return 内部表单 Map，可能为 {@code null}；返回的是可变引用
     */
    public Map<String, String> getForm() {
        return form;
    }

    /**
     * 获取鉴权提供者
     *
     * @return 鉴权提供者；未设置时为 {@code null}
     */
    public AuthProvider getAuthProvider() {
        return authProvider;
    }


    // --------------------------------- invoke ---------------------------------

    /**
     * 初始化 HttpURLConnection
     *
     * <p>
     * 依次完成 URL/form 拼接、认证应用、连接创建、HTTPS 信任配置、超时与请求头设置，
     * 并在建立连接后写入非 GET 请求的请求体。连接出错时断开并包装为 {@link RuntimeException}。
     * </p>
     *
     * @return 已连接、可读取响应的 HttpURLConnection
     * @throws RuntimeException URL/方法/Content-Type 缺失，或连接建立、请求体写入失败时抛出，消息中包含请求 URL
     */
    private HttpURLConnection prepareConnection() {
        // valid
        AssertTool.isTrue(StringTool.isNotBlank(this.url), "http-request url is null");
        AssertTool.isTrue(Objects.nonNull(this.method), "http-request method is null");
        AssertTool.isTrue(Objects.nonNull(this.contentType), "http-request contentType is null");

        // init connection
        HttpURLConnection connection = null;
        try {
            // parse url
            String finalUrl = this.url;
            if (Method.GET == this.method) {
                // write form-data to url (only GET method)
                String formParam = HttpTool.generateUrlParam(this.form);
                if (StringTool.isNotBlank(formParam)) {
                    finalUrl = finalUrl + (finalUrl.contains("?") ? "&" : "?") + formParam;
                }
            }

            // auth provider: may modify header and url
            if (this.authProvider != null) {
                this.authProvider.apply(this);
            }

            // open conection
            URL finalUrlURL = new URL(finalUrl);
            connection = (HttpURLConnection) finalUrlURL.openConnection();

            // default: trust-https
            if (isHttps(finalUrl)) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                trustAllHosts(https);
            }

            // default: setting
            connection.setDoOutput(true);       // allow write data to url connection (output: local -> remote)
            connection.setDoInput(true);        // allow read data from url connection (input: remote -> local)
            connection.setRequestProperty(Header.CONNECTION.getValue(), "Keep-Alive");
            connection.setRequestProperty(Header.ACCEPT_CHARSET.getValue(), StandardCharsets.UTF_8.toString());

            // custome: setting
            connection.setRequestMethod(this.method.toString());
            connection.setRequestProperty(Header.CONTENT_TYPE.getValue(), this.contentType.getValue(StandardCharsets.UTF_8));
            connection.setReadTimeout(this.readTimeout);
            connection.setConnectTimeout(this.connectTimeout);
            connection.setUseCaches(this.useCaches);

            // custome: header
            if (MapTool.isNotEmpty(this.headers)) {
                for (String key : this.headers.keySet()) {
                    connection.setRequestProperty(key, this.headers.get(key));
                }
            }

            // custome: cookie
            if (MapTool.isNotEmpty(this.cookies)) {
                String cookieString = getCookieString();
                connection.setRequestProperty(Header.COOKIE.getValue(), cookieString);
            }

            // interceptor before（注意：此时请求头已写入连接，before 中新增的 header 不会生效）
            if (CollectionTool.isNotEmpty(this.interceptors)) {
                for (HttpInterceptor interceptor : this.interceptors) {
                    interceptor.before(this);
                }
            }

            // do connection
            connection.connect();

            // write request-body (all methods except GET)
            if (Method.GET != this.method) {
                String requestBody = null;
                if (StringTool.isNotBlank(this.body)) {
                    requestBody = this.body;
                } else if (MapTool.isNotEmpty(this.form)) {
                    requestBody = HttpTool.generateUrlParam(this.form);
                }

                // write body-data (all methods except GET): output = local -> remote
                if (StringTool.isNotBlank(requestBody)) {
                    try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
                        dataOutputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                        dataOutputStream.flush();
                    }
                }
            }

            return connection;
        } catch (Exception e) {
            // disconnect when error
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
            throw new RuntimeException("Http Request Error (" + e.getMessage() + "). for url : " + url, e);
        }
    }

    /**
     * send http request
     *
     * <p>
     * 发起请求并将响应体按 UTF-8 读入 {@link HttpResponse}（状态码非 200 时尝试读取 errorStream，可能为空）；
     * 同时解析 {@code Set-Cookie} 响应头中的 name/value（忽略 Domain/Path/Expires 等属性）。
     * 响应流与连接均由本方法关闭/断开。
     * </p>
     *
     * @return 包含状态码、响应体、URL 与响应 Cookie 的响应对象，非 {@code null}（任何状态码都会返回）
     * @throws RuntimeException 连接失败、超时、IO 异常或拦截器抛异常时抛出，原始异常作为 cause，消息中包含请求 URL
     */
    public HttpResponse execute() {
        HttpURLConnection connection = null;
        try {
            // prepare connection
            connection = prepareConnection();

            // init httpResponse
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setUrl(this.url);
            httpResponse.setStatusCode(connection.getResponseCode());

            // write response-body （ parse from inputStream, consider error )
            InputStream inputStream = httpResponse.isSuccess()
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            if (inputStream != null) {
                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    httpResponse.setResponse(result.toString());
                }
            }

            // write set-cookie from response
            httpResponse.setCookies(parseResponseCookieData(connection));

            // interceptor after
            if (CollectionTool.isNotEmpty(this.interceptors)) {
                for (HttpInterceptor interceptor : this.interceptors) {
                    interceptor.after(this, httpResponse);
                }
            }

            return httpResponse;
        } catch (Exception e) {
            throw new RuntimeException("Http Request Error (" + e.getMessage() + "). for url : " + url, e);
        } finally {
            // disconnect when finish
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * download file, save to destFile
     *
     * @param parentPath    parent path
     * @param destFile      dest file
     * @return 下载写入的字节数
     * @throws RuntimeException 目标路径为目录、响应状态码非 200、IO 失败时抛出；异常消息包含请求 URL
     */
    public long download(String parentPath, String destFile) {
        return download(FileTool.file(parentPath, destFile).getPath());
    }

    /**
     * download file, save to destFile
     *
     * <p>
     * 目标文件已存在时先删除再下载，父目录不存在时自动创建；仅接受状态码 200。
     * 下载过程中发生 IO 异常会删除目标文件；由其他异常导致的残留文件不会被清理。
     * 连接与流由本方法关闭。
     * </p>
     *
     * @param destFile destFile
     * @return file size {@code IOTool.copy} 返回的复制字节数
     * @throws RuntimeException 若 destFile 是目录、目标路径不可写、状态码非 200 或 IO 失败
     */
    public long download(String destFile) {
        HttpURLConnection connection = null;
        try {
            File destFileNew = FileTool.file(destFile);
            // valid destFilePath
            if (FileTool.isDirectory(destFileNew)) {
                throw new RuntimeException("Download fail, destFile is a directory for url : " + url);
            }
            if (FileTool.exists(destFileNew)) {
                // default overwrite file when exist
                FileTool.delete(destFileNew);
                //throw new RuntimeException("Download fail, file already exists for url : " + url);
            }
            FileTool.createParentDirectories(destFileNew);

            // prepare connection
            connection = prepareConnection();

            // validate statusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Download fail, statusCode(" + statusCode + ") for url : " + url);
            }

            // download to file: copy remote -> local
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(destFileNew)) {
                return IOTool.copy(in, out, false, true);
            }
        } catch (IOException e) {
            // delete file when error
            if (FileTool.isFile(destFile)) {
                FileTool.delete(destFile);
            }
            throw new RuntimeException("Download error for url : " + url, e);
        } finally {
            // disconnect when finish
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    // --------------------------------- tool ---------------------------------

    /**
     * parse response-cookie from HttpURLConnection
     *
     * <p>
     * 只提取每个 Set-Cookie 首段的 {@code name=value}，忽略 Path、Domain、Expires、HttpOnly 等属性；
     * 同名 Cookie 后出现的值覆盖先出现的值。
     * </p>
     *
     * @param connection    connection of HttpURLConnection
     * @return cookieMap；无 Set-Cookie 时返回 {@code null}
     */
    private Map<String, String> parseResponseCookieData(HttpURLConnection connection) {
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> setCookieHeaders = headerFields.get(Header.SET_COOKIE.getValue());
        if (CollectionTool.isEmpty(setCookieHeaders)) {
            return null;
        }

        Map<String, String> cookieMap = new HashMap<>();
        for (String setCookieHeader : setCookieHeaders) {
            String[] parts = setCookieHeader.split(";");
            if (parts.length > 0) {
                String cookiePart = parts[0];
                String[] cookieKeyValue = cookiePart.split("=", 2);
                if (cookieKeyValue.length == 2) {
                    cookieMap.put(cookieKeyValue[0], cookieKeyValue[1]);
                }
            }
        }
        return cookieMap;
    }

    /**
     * trust-https
     *
     * <p>
     * <b>安全警告：</b>安装信任所有证书的 {@link SSLSocketFactory}，并接受任意主机名，
     * 会失去对服务端身份与证书链的校验能力，仅适用于测试或受控内网。
     * </p>
     */
    private void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();

            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    /**
     * trust-all-certs
     */
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};

}