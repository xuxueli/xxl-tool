//package com.xxl.tool.deprecated.httprpc;
//
//import com.xxl.tool.core.StringTool;
//import com.xxl.tool.http.HttpTool;
//
//import java.lang.annotation.*;
//import java.lang.reflect.Method;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Proxy;
//import java.lang.reflect.Type;
//
///**
// * http-rpc client
// *
// * <pre>
// *      // 《Client 使用示例》
// *      // 1、开发：业务服务接口
// *      @HttpRpcClient.HttpRpcService(url = "http://localhost:8080/jsonrpc", path = "/demoService", timeout = 3000)
// *      public interface DemoService {
// *
// *          public DemoResp test(DemoReq req);
// *          public Response<DemoResp> test2(DemoReq req);
// *          public Response<DemoResp> test3(DemoReq req, DemoReq req2);
// *          // 最终请求地址：{url}/{接口path}/{方法path}
// *          @HttpRpcClient.HttpRpcMethod(path = "/test4", timeout = 5000);
// *          public void a11111();
// *      }
// *
// *      // 2、配置：客户端代理
// *      DemoService demoService = HttpRpcClient.create().proxy(DemoService.class);
// *      DemoService demoService = HttpRpcClient.create().url("http://localhost:8080/jsonrpc").proxy(DemoService.class);
// *      DemoService demoService = HttpRpcClient.create().servicePath("/demoService").proxy(DemoService.class);
// *      DemoService demoService = HttpRpcClient.create().timeout(3000).proxy(DemoService.class);
// *      DemoService demoService = HttpRpcClient.create().url("http://localhost:8080/jsonrpc").servicePath("/demoService").timeout(3000).proxy(DemoService.class);
// *
// *      // 3、发送请求
// *      DemoResp demoResp = demoService2.test(new DemoReq("jack"));
// * </pre>
// */
//public class HttpRpcClient {
//
//    // ---------------------- build ----------------------
//
//    /**
//     * build http-rpc client
//     *
//     * @return client
//     */
//    public static HttpRpcClient create(){
//        return new HttpRpcClient();
//    }
//
//    // ---------------------- field ----------------------
//
//    private String url;
//    private String servicePath;
//    private int timeout = 3000;
//    private String auth;
//
//    /**
//     * set base url
//     *
//     * @param url base url
//     * @return this
//     */
//    public HttpRpcClient url(String url) {
//        this.url = url;
//        return this;
//    }
//
//    /**
//     * set service path
//     *
//     * @param servicePath service path
//     * @return this
//     */
//    public HttpRpcClient servicePath(String servicePath) {
//        this.servicePath = servicePath;
//        return this;
//    }
//
//    /**
//     * set timeout
//     *
//     * @param timeout  by milliseconds
//     * @return this
//     */
//    public HttpRpcClient timeout(int timeout) {
//        this.timeout = timeout;
//        return this;
//    }
//
//    public HttpRpcClient auth(String auth) {
//        this.auth = auth;
//        return this;
//    }
//
//    // get
//
//    public String getUrl() {
//        return url;
//    }
//
//    public String getServicePath() {
//        return servicePath;
//    }
//
//    public int getTimeout() {
//        return timeout;
//    }
//
//    public String getAuth() {
//        return auth;
//    }
//
//    // ---------------------- proxy ----------------------
//
//    /**
//     * http-rpc client proxy
//     *
//     * @param serviceInterface  the service interface
//     * @return  the proxy instance
//     * @param <T>   the service interface type
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T proxy(Class<T> serviceInterface) {
//        return (T) Proxy.newProxyInstance(
//                serviceInterface.getClassLoader(),
//                new Class[]{serviceInterface},
//                (proxy, method, args) -> invoke(serviceInterface, method, args));
//    }
//
//    /**
//     * http-rpc invoke
//     */
//    @SuppressWarnings("unchecked")
//    private <T> T invoke(final Class<T> serviceInterface,
//                         final Method method,
//                         final Object[] params) {
//
//        // parse base data
//        HttpRpcService httpRpcService = serviceInterface.getAnnotation(HttpRpcService.class);
//        HttpRpcMethod httpRpcMethod = method.getAnnotation(HttpRpcMethod.class);
//        Type responseType = method.getGenericReturnType();
//
//        // parse baseUrl: api > service-annotation
//        String baseUrl = url;
//        if (StringTool.isBlank(baseUrl)) {
//            if (httpRpcService!=null && StringTool.isNotBlank(httpRpcService.url())) {
//                baseUrl = httpRpcService.url();
//            }
//        }
//        if (StringTool.isBlank(baseUrl)) {
//            throw new RuntimeException("http-rpc client invoke fail, baseUrl is null");
//        }
//        // 如果尾部是/，去除/
//        baseUrl = removeSuffix(baseUrl, "/");
//
//        // parse servicePath: api > service-annotation > service-class#getSimpleName
//        String servicePathFinal = parseServicePath(servicePath, httpRpcService, serviceInterface);
//
//        // parse methodPath: method-annotation > method-name
//        String methodPathFinal = parseMethodPath(httpRpcMethod, method);
//
//        /**
//         * build final-url
//         *
//         * <pre>
//         *     URL格式： {baseUrl}/{servicePath}/{methodPath}
//         *     示例：
//         *      baseUrl = http://127.0.0.1:8080
//         *      servicePath = user
//         *      methodPath = getById
//         *
//         *      => http://127.0.0.1:8080/user/getById
//         * </pre>
//         */
//        String finalUrl = baseUrl + "/" + servicePathFinal + "/" +  methodPathFinal;
//
//        // parse timeout: api > service-annotation < method-annotation <
//        int methodTimeout = -1;
//        if (httpRpcService!=null && httpRpcService.timeout()>0) {
//            methodTimeout = httpRpcService.timeout();
//        }
//        if (httpRpcMethod!=null && httpRpcMethod.timeout()>0) {
//            methodTimeout = httpRpcMethod.timeout();
//        }
//        if (timeout>0) {
//            methodTimeout = timeout;
//        }
//        if (methodTimeout <=0) {
//            throw new RuntimeException("http-rpc client invoke fail, timeout invalid");
//        }
//
//        // parse request object
//        Object request = null ;
//        if (params !=null) {
//            /**
//             * 注意：
//             * 1、直接请求 springmvc#controller 时：此时springmvc负责路由匹配，通过 @RequestBody 获取请求对象，限制入参最多只有一个；
//             * 2、请求转发至 HttpRpcServer 时：此时 HttpRpcServer 负责路由匹配，不限制入参数量；
//             */
//            request = params.length==1 ? params[0] : params;
//        }
//
//        // parse response type
//        Class<T> typeOfT = null;
//        Type[] typeArguments = null;
//        if (responseType instanceof ParameterizedType) {
//            typeOfT = (Class<T>) ((ParameterizedType)responseType).getRawType();
//            typeArguments = ((ParameterizedType)responseType).getActualTypeArguments();
//        } else {
//            typeOfT = (Class<T>) responseType;
//        }
//
//        // do execute
//        return HttpTool
//                .createPost(finalUrl)
//                .connectTimeout(methodTimeout)
//                .readTimeout(methodTimeout)
//                .request(request)
//                .auth(auth)
//                .execute()
//                .response(typeOfT, typeArguments);
//    }
//
//    /**
//     * parse servicePath: api > service-annotation > service-class#getSimpleName
//     *
//     * @param servicePath       servicePath
//     * @param httpRpcService    service-annotation
//     * @param serviceInterface  service-class
//     * @return servicePathFinal
//     */
//    public static String parseServicePath(String servicePath, HttpRpcService httpRpcService, Class<?> serviceInterface) {
//        String servicePathFinal = servicePath;
//
//        if (StringTool.isBlank(servicePathFinal)) {
//            if (httpRpcService!=null && StringTool.isNotBlank(httpRpcService.path())) {
//                servicePathFinal = httpRpcService.path();
//            } else {
//                servicePathFinal = StringTool.lowerCaseFirst(serviceInterface.getSimpleName());
//            }
//        }
//
//        servicePathFinal = removePrefix(servicePathFinal, "/");
//        servicePathFinal = removeSuffix(servicePathFinal, "/");
//        return servicePathFinal;
//    }
//
//    /**
//     * parse methodPath: method-annotation > method-name
//     *
//     * @param httpRpcMethod method-annotation
//     * @param method        method
//     * @return  methodPath
//     */
//    public static String parseMethodPath(HttpRpcMethod httpRpcMethod, Method method) {
//        String methodPath = (httpRpcMethod!=null && StringTool.isNotBlank(httpRpcMethod.path()))
//                ?httpRpcMethod.path()
//                :method.getName();
//
//        methodPath = removePrefix(methodPath, "/");
//        methodPath = removeSuffix(methodPath, "/");
//        return methodPath;
//    }
//
//    // ---------------------- annotation ----------------------
//
//    /**
//     * http-rpc method anotation
//     */
//    @Target({ElementType.METHOD})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Inherited
//    public @interface HttpRpcMethod {
//
//        /**
//         * method-path, default "/method-name"
//         */
//        String path() default "";
//
//        int timeout() default -1;
//
//    }
//
//    /**
//     * http-rpc service anotation
//     */
//    @Target({ElementType.TYPE})
//    @Retention(RetentionPolicy.RUNTIME)
//    @Inherited
//    public @interface HttpRpcService {
//
//        /**
//         * request base url
//         *
//         * <pre>
//         *     // 请求地址格式：最终请求地址 =  baseUrl + servicePath + methodPath
//         *     base url: http:www.xxx.com
//         *     service path : /aaa
//         *     method path: /bbb
//         *
//         *     => final url: http:www.xxx.com/aaa/bbb
//         * </pre>
//         */
//        String url() default "";
//
//        /**
//         * service-path, default "/service-class#getSimpleName"
//         */
//        String path() default "";
//
//        /**
//         * timeout
//         */
//        int timeout() default -1;
//
//    }
//
//    // ---------------------- tool ----------------------
//
//    /**
//     * remove suffix
//     */
//    public static String removeSuffix(String str, String suffix) {
//        if (str == null || StringTool.isBlank(suffix)) {
//            return str;
//        }
//        if (str.endsWith(suffix)) {
//            return str.substring(0, str.length() - suffix.length());
//        }
//        return str;
//    }
//
//    /**
//     * remove prefix
//     */
//    public static String removePrefix(String str, String prefix) {
//        if (str == null || StringTool.isBlank(prefix)) {
//            return str;
//        }
//        if (str.startsWith(prefix)) {
//            return str.substring(prefix.length());
//        }
//        return str;
//    }
//
//}