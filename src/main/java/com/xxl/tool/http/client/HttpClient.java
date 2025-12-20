package com.xxl.tool.http.client;

import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.http.http.HttpRequest;
import com.xxl.tool.http.http.enums.ContentType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * http client
 */
public class HttpClient {

    // ----------- field -----------

    private String url;                                     // 请求 Url
    private Map<String, String> headers;                    // 存储请求头
    private Map<String, String> cookies;                    // Cookie（需要格式转换）
    private int timeout = 3 * 1000;                         // connectTimeout、readTimeout，单位：ms
    private String auth;                                    // 鉴权信息

    // ----------- set build -----------

    /**
     * 设置URL
     */
    public HttpClient url(String url) {
        this.url = url;
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
     * 超时时间
     */
    public HttpClient timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置鉴权信息
     */
    public HttpClient auth(String auth) {
        this.auth = auth;
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
                         final Method method,
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

        // parse timeout: api < service-annotation < method-annotation
        int methodTimeout = -1;
        if (httpClientMethod != null && httpClientMethod.timeout() > 0) {
            methodTimeout = httpClientMethod.timeout();
        } else if (httpClientService != null && httpClientService.timeout() > 0) {
            methodTimeout = httpClientService.timeout();
        } else if (timeout > 0) {
            methodTimeout = timeout;
        }
        if (methodTimeout <=0) {
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
        HttpRequest httpRequest = HttpTool
                .createPost(finalUrl)
                .contentType(ContentType.JSON)
                .header(headers)
                .cookie(cookies)
                .connectTimeout(methodTimeout)
                .readTimeout(methodTimeout)
                .useCaches(false)
                .auth(auth)
                .request(request);

        // do execute
        if (typeOfT != null) {
            return httpRequest
                    .execute()
                    .response(typeOfT, typeArguments);
        } else {
            httpRequest.execute();
            return null;
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