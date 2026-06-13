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
 * http client
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
     */
    public HttpClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置 Method
     */
    public HttpClient method(com.xxl.tool.http.http.enums.Method method) {
        this.method = method;
        return this;
    }

    /**
     * 设置 Content-Type
     */
    public HttpClient contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置请求头，覆盖更新
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
     */
    public HttpClient connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     */
    public HttpClient readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 设置是否使用缓存
     */
    public HttpClient useCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    /**
     * 设置请求体 body
     */
    public HttpClient body(String body) {
        this.body = body;
        return this;
    }

    /**
     * 设置表单 form
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
     */
    public HttpClient auth(AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    /**
     * 设置 Basic Auth
     */
    public HttpClient basicAuth(String username, String password) {
        this.authProvider = new BasicAuthProvider(username, password);
        return this;
    }

    /**
     * 设置 Bearer Token Auth
     */
    public HttpClient bearerAuth(String token) {
        this.authProvider = new BearerTokenAuthProvider(token);
        return this;
    }

    /**
     * 设置 API Key Auth
     */
    public HttpClient apiKeyAuth(String keyName, String keyValue, ApiKeyAuthProvider.Location location) {
        this.authProvider = new ApiKeyAuthProvider(keyName, keyValue, location);
        return this;
    }

    /**
     * 设置拦截器，覆盖更新
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
     * @param serviceInterface  the service interface
     * @return  the proxy instance
     * @param <T>   the service interface type
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

        // parse servicePath: service-annotation > service-class#getSimpleName
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
     * @param httpRpcService    service-annotation
     * @param serviceInterface  service-class
     * @return servicePathFinal
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
     * @param httpRpcMethod method-annotation
     * @param method        method
     * @return  methodPath
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