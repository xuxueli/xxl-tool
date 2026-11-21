package com.xxl.tool.http.client;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.http.http.auth.AuthProvider;
import com.xxl.tool.http.http.auth.impl.ApiKeyAuthProvider;
import com.xxl.tool.http.http.auth.impl.BasicAuthProvider;
import com.xxl.tool.http.http.auth.impl.BearerTokenAuthProvider;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.ContentType;
import com.xxl.tool.http.http.iface.HttpInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * http client：可链式配置的 HTTP 客户端，并支持将接口代理为 HTTP 调用
 *
 * <p>
 * 两种用法：
 * </p>
 * <ul>
 *   <li>配置后通过 {@link #proxy(Class)} 生成接口代理：请求路径、超时可由
 *       {@link HttpClientService}/{@link HttpClientMethod} 注解声明；</li>
 *   <li>作为 {@link HttpRequest} 的配置模板：代理调用时每次都会新建请求并在结束后断开连接。</li>
 * </ul>
 *
 * <p>
 * 默认方法为 POST、Content-Type 为 JSON、连接与读取超时均为 3000ms、不使用缓存；
 * 响应体按 UTF-8 解析为方法返回类型（void 方法忽略响应体）。
 * 重定向、代理与 HTTPS 信任策略沿用 {@link HttpRequest} 的默认行为（HTTPS 默认信任所有证书）。
 * </p>
 *
 * <p>
 * 本类为可变配置对象，<b>非线程安全</b>；请在初始化阶段完成配置，之后只读复用。
 * 生成的代理对象本身不持有可变请求状态，但共享同一客户端配置。
 * </p>
 */
public class HttpClient {

    // ----------- field -----------

    private String url;                                     // 请求 Url
    private com.xxl.tool.http.http.enums.Method method = com.xxl.tool.http.http.enums.Method.POST;                    // Method
    private ContentType contentType = ContentType.JSON;     // Content-Type
    private Map<String, String> headers;                    // 存储请求头
    private Map<String, String> cookies;                    // Cookie（需要格式转换）
    private int connectTimeout = 3 * 1000;                  // 连接超时时间，单位：ms
    private int readTimeout = 3 * 1000;                     // 读取超时时间，单位：ms
    private boolean useCaches = false;                      // 是否使用缓存
    private String body;                                    // 请求体
    private Map<String, String> form;                       // 表单数据
    private AuthProvider authProvider;                      // 鉴权提供者
    private List<HttpInterceptor> interceptors;             // HttpInterceptor

    // ----------- set build -----------

    /**
     * 设置URL
     *
     * <p>
     * 作为代理调用的 base URL，优先级高于 {@link HttpClientService#url()}；为空时回退到注解配置。
     * </p>
     *
     * @param url 基础 URL，可为 {@code null}
     * @return this
     */
    public HttpClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置 Method
     *
     * @param method 请求方法，默认 POST；{@code null} 会在执行时导致请求失败
     * @return this
     */
    public HttpClient method(com.xxl.tool.http.http.enums.Method method) {
        this.method = method;
        return this;
    }

    /**
     * 设置 Content-Type
     *
     * @param contentType 请求内容类型，默认 JSON；{@code null} 会在执行时导致请求失败
     * @return this
     */
    public HttpClient contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置请求头，覆盖更新
     *
     * <p>
     * 先清空已有请求头再逐项写入；入参为 {@code null} 或空 Map 时不修改现有配置。
     * </p>
     *
     * @param header 请求头 Map，可为 {@code null}
     * @return this
     */
    public HttpClient header(Map<String, String> header) {
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
     * 同名 key 覆盖旧值；key 为空白或 value 为 {@code null} 时忽略。
     * </p>
     *
     * @param key   请求头名称，为空白时忽略
     * @param value 请求头值，为 {@code null} 时忽略
     * @return this
     */
    public HttpClient header(String key, String value) {
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
     * 设置Cookie，覆盖更新
     *
     * <p>
     * 先清空已有 Cookie 再逐项写入；入参为 {@code null} 或空 Map 时不修改现有配置。
     * </p>
     *
     * @param cookie Cookie Map，可为 {@code null}
     * @return this
     */
    public HttpClient cookie(Map<String, String> cookie) {
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
     * 同名 key 覆盖旧值；key 为空白或 value 为 {@code null} 时忽略。
     * </p>
     *
     * @param key   Cookie 名称，为空白时忽略
     * @param value Cookie 值，为 {@code null} 时忽略
     * @return this
     */
    public HttpClient cookie(String key, String value) {
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
     * @param connectTimeout 连接超时时间，单位：毫秒；{@code <= 0} 会导致代理调用失败
     * @return this
     */
    public HttpClient connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 读取超时时间，单位：毫秒；{@code <= 0} 会导致代理调用失败
     * @return this
     */
    public HttpClient readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 设置是否使用缓存
     *
     * @param useCaches 是否允许底层连接使用缓存，默认 {@code false}
     * @return this
     */
    public HttpClient useCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    /**
     * 设置请求体 body
     *
     * <p>
     * 原样作为所有代理调用的请求体发送；若代理方法传入了非空参数，参数会被序列化为 JSON 并覆盖该 body。
     * </p>
     *
     * @param body 请求体字符串，可为 {@code null}
     * @return this
     */
    public HttpClient body(String body) {
        this.body = body;
        return this;
    }

    /**
     * 设置表单 form
     *
     * @param key   表单字段名，为空白时忽略
     * @param value 表单字段值，为 {@code null} 时忽略
     * @return this
     */
    public HttpClient form(String key, String value) {
        if (StringTool.isBlank(key) || Objects.isNull(value)) {
            return this;
        }
        if (null == this.form) {
            this.form = new HashMap<>();
        }
        this.form.put(key, value);
        return this;
    }

    /**
     * 设置表单 form，覆盖更新
     *
     * <p>
     * 先清空已有表单再逐项写入；入参为 {@code null} 或空 Map 时不修改现有配置。
     * </p>
     *
     * @param form 表单 Map，可为 {@code null}
     * @return this
     */
    public HttpClient form(Map<String, String> form) {
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
     * 传入 {@code null} 表示不认证；后设置的认证方式覆盖之前的设置。
     * </p>
     *
     * @param authProvider 鉴权提供者，可为 {@code null}
     * @return this
     */
    public HttpClient auth(AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    /**
     * 设置 Basic Auth
     *
     * @param username 用户名，可为 {@code null}
     * @param password 密码，可为 {@code null}
     * @return this
     */
    public HttpClient basicAuth(String username, String password) {
        this.authProvider = new BasicAuthProvider(username, password);
        return this;
    }

    /**
     * 设置 Bearer Token Auth
     *
     * @param token 访问令牌，可为 {@code null}
     * @return this
     */
    public HttpClient bearerAuth(String token) {
        this.authProvider = new BearerTokenAuthProvider(token);
        return this;
    }

    /**
     * 设置 API Key Auth
     *
     * @param keyName  API Key 名称（头名或参数名）
     * @param keyValue API Key 值
     * @param location 放置位置，{@code null} 时按 QUERY 处理
     * @return this
     */
    public HttpClient apiKeyAuth(String keyName, String keyValue, ApiKeyAuthProvider.Location location) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, location);
        return this;
    }

    /**
     * 设置拦截器，覆盖更新
     *
     * <p>
     * 先清空已有拦截器再按列表顺序添加；入参为 {@code null} 或空列表时不修改现有配置。
     * </p>
     *
     * @param interceptors 拦截器列表，可为 {@code null}
     * @return this
     */
    public HttpClient interceptor(List<HttpInterceptor> interceptors) {
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
    public HttpClient interceptor(HttpInterceptor interceptor) {
        if (Objects.isNull(interceptor)) {
            return this;
        }
        if (null == interceptors) {
            this.interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        return this;
    }

    // ---------------------- proxy ----------------------

    /**
     * http client proxy
     *
     * <p>
     * 为接口生成动态代理，接口方法调用会被转换为 HTTP 请求：URL 由 baseUrl/服务路径/方法路径拼接，
     * 返回值按方法泛型反序列化，{@code void} 方法忽略响应体。
     * {@code toString}/{@code hashCode}/{@code equals} 走本地对象语义，其余方法（包括接口 default 方法）
     * 均按 HTTP 调用处理。
     * </p>
     *
     * @param serviceInterface  the service interface，必须是接口
     * @return  the proxy instance 代理实例，非 {@code null}
     * @param <T>   the service interface type 接口类型
     * @throws IllegalArgumentException 当 {@code serviceInterface} 不是接口或为 {@code null} 时
     */
    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                (proxy, method, args) -> {

                    // exclude object methods
                    String methodName = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        if ("toString".equals(methodName)) {
                            return this.toString();
                        } else if ("hashCode".equals(methodName)) {
                            return this.hashCode();
                        }
                    } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
                        return this.equals(args[0]);
                    }

                    // invoke biz method
                    return invoke(serviceInterface, method, args);
                });
    }

    /**
     * http client invoke
     *
     * <p>
     * 解析注解与服务路径，拼装最终 URL，按“方法注解 > 服务注解 > 客户端默认值”确定超时，
     * 将单个参数或参数数组作为请求体序列化，执行请求并按返回类型反序列化响应。
     * </p>
     *
     * @param serviceInterface 代理接口
     * @param method           被调用的接口方法
     * @param params           方法实参，可为 {@code null}
     * @param <T>              返回类型
     * @return 反序列化后的响应；{@code void} 方法返回 {@code null}
     * @throws RuntimeException baseUrl 为空、超时非正数、请求执行失败或响应解析失败时抛出
     */
    @SuppressWarnings("unchecked")
    private <T> T invoke(final Class<T> serviceInterface,
                         final java.lang.reflect.Method method,
                         final Object[] params) {

        // parse base data
        HttpClientService httpClientService = serviceInterface.getAnnotation(HttpClientService.class);
        HttpClientMethod httpClientMethod = method.getAnnotation(HttpClientMethod.class);
        Type responseType = method.getGenericReturnType();

        // parse baseUrl: api < service-annotation
        String baseUrl = url;
        if (StringTool.isBlank(baseUrl)) {
            if (httpClientService!=null && StringTool.isNotBlank(httpClientService.url())) {
                baseUrl = httpClientService.url();
            }
        }
        if (StringTool.isBlank(baseUrl)) {
            throw new RuntimeException("http client invoke fail, baseUrl is null");
        }
        // 去除尾部/
        baseUrl = StringTool.removeSuffix(baseUrl, "/");

        // parse servicePath: service-annotation only
        String servicePathFinal = parseServicePath(httpClientService, serviceInterface);

        // parse methodPath: method-annotation > method-name
        String methodPathFinal = parseMethodPath(httpClientMethod, method);

        /**
         * build final-url
         *
         * <pre>
         *     URL格式： {baseUrl}/{servicePathFinal}/{methodPath}
         *     示例：
         *      baseUrl = http://127.0.0.1:8080
         *      servicePath = user
         *      methodPath = getById
         *      => http://127.0.0.1:8080/user/getById
         * </pre>
         */
        String finalUrl = StringTool.isNotBlank(servicePathFinal)
                ? (baseUrl + "/" + servicePathFinal + "/" +  methodPathFinal)
                : (baseUrl  + "/" +  methodPathFinal);

        // parse timeout: method-annotation > service-annotation > api
        int connectTimeoutFinal = connectTimeout;
        int readTimeoutFinal = readTimeout;
        if (httpClientMethod != null && httpClientMethod.timeout() > 0) {
            connectTimeoutFinal = httpClientMethod.timeout();
            readTimeoutFinal = httpClientMethod.timeout();
        } else if (httpClientService != null && httpClientService.timeout() > 0) {
            connectTimeoutFinal = httpClientService.timeout();
            readTimeoutFinal = httpClientService.timeout();
        }
        if (connectTimeoutFinal <=0 || readTimeoutFinal <=0) {
            throw new RuntimeException("http client invoke fail, timeout invalid");
        }

        // parse request object
        Object request = null ;
        if (params !=null) {
            request = params.length==1 ? params[0] : params;
        }

        // parse response type
        Class<T> typeOfT = null;
        Type[] typeArguments = null;
        if (responseType instanceof ParameterizedType) {                        // 泛型
            typeOfT = (Class<T>) ((ParameterizedType)responseType).getRawType();
            typeArguments = ((ParameterizedType)responseType).getActualTypeArguments();
        } else if (responseType == void.class) {                                // void
            typeOfT = null;
        } else  {
            typeOfT = (Class<T>) responseType;
        }

        // build request
        HttpRequest httpRequest = new HttpRequest()
                .url(finalUrl)
                .method(this.method)
                .contentType(contentType)
                .header(headers)
                .cookie(cookies)
                .connectTimeout(connectTimeoutFinal)
                .readTimeout(readTimeoutFinal)
                .useCaches(useCaches)
                .body(body)
                .form(form)
                .auth(authProvider)
                .interceptor(interceptors)
                .request(request);

        // do execute
        if (typeOfT != null) {
            return httpRequest
                    .execute()
                    .response(typeOfT, typeArguments);
        } else {
            return httpRequest
                    .execute()
                    .response(null);
        }
    }

    /**
     * parse servicePath: service-annotation
     *
     * <p>
     * 仅取 {@link HttpClientService#path()}，自动去除首尾 {@code /}；
     * 未标注或 path 为空白时返回 {@code null}（不使用类名推断）。
     * </p>
     *
     * @param httpRpcService    service-annotation，可为 {@code null}
     * @param serviceInterface  service-class（当前实现未使用）
     * @return servicePathFinal；无有效 path 时为 {@code null}
     */
    public static String parseServicePath(HttpClientService httpRpcService, Class<?> serviceInterface) {
        String servicePathFinal = null;
        if (httpRpcService!=null && StringTool.isNotBlank(httpRpcService.path())) {
            servicePathFinal = httpRpcService.path();
        }
        /*else {
            servicePathFinal = StringTool.lowerCaseFirst(serviceInterface.getSimpleName());
        }*/

        servicePathFinal = StringTool.removePrefix(servicePathFinal, "/");
        servicePathFinal = StringTool.removeSuffix(servicePathFinal, "/");
        return servicePathFinal;
    }

    /**
     * parse methodPath: method-annotation > method-name
     *
     * <p>
     * 取 {@link HttpClientMethod#path()}，为空时使用方法名，并自动去除首尾 {@code /}。
     * </p>
     *
     * @param httpRpcMethod method-annotation，可为 {@code null}
     * @param method        method，不能为 {@code null}
     * @return  methodPath，非 {@code null}
     */
    public static String parseMethodPath(HttpClientMethod httpRpcMethod, Method method) {
        String methodPath = (httpRpcMethod!=null && StringTool.isNotBlank(httpRpcMethod.path()))
                ?httpRpcMethod.path()
                :method.getName();

        methodPath = StringTool.removePrefix(methodPath, "/");
        methodPath = StringTool.removeSuffix(methodPath, "/");
        return methodPath;
    }

    // ---------------------- tool ----------------------


}