//package com.xxl.tool.deprecated.httprpc;
//
//import com.google.gson.JsonElement;
//import com.xxl.tool.core.StringTool;
//import com.xxl.tool.gson.GsonTool;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.lang.reflect.Method;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * http-rpc server
// *
// * <pre>
// *     // 《Server 使用示例》
// *
// *     // 方式一：与Web（如SpringMVC）系统集成
// *
// *     // 1.1、开发：业务接口服务
// *     public static class DemoServiceImpl implements DemoService {
// *         ……
// *     }
// *
// *     // 1.2、注册：业务接口服务
// *     private static HttpRpcServer httpRpcServer;
// *     static {
// *         httpRpcServer = HttpRpcServer.create();
// *         httpRpcServer.register(new DemoServiceImpl());
// *         httpRpcServer.register("demoService2", new DemoService2Impl());
// *     }
// *
// *     // 1.3.1、集成方式01：序列化内部实现（更闭环）；
// *     @RequestMapping("/xxx/{service}/{method}")
// *     @ResponseBody
// *     public String invokeWithResult(@PathVariable String service, @PathVariable String method, @RequestBody(required = false) String data) throws Exception {
// *         return httpRpcServer.invoke(service, method, data);
// *     }
// *
// *     // 1.3.2、集成方式01：序列化委托Web框架内部实现（定制方便）；
// *     @RequestMapping("/xxx/{service}/{method}")
// *     @ResponseBody
// *     public Object invoke(@PathVariable String service, @PathVariable String method, @RequestBody(required = false) String data) throws Exception {
// *         return httpRpcServer.invokeWithResult(service, method, data);
// *     }
// *
// *     // 方式二：HttpRpcClient 直接请求 SpringMVC#Controller 接口（不使用本组件）
// *
// *     // 直接开发单个 Controller 接口；
// *     // 需要注意：该方式通过 @RequestBody 获取请求对象，限制入参最多只有一个；
// *     @RequestMapping("/xxx/demoService")
// *     public class JsonRpcController3 {
// *
// *          @RequestMapping("/test2")
// *          @ResponseBody
// *          public Response<DemoResp> test2(@RequestBody(required = false)DemoReq req) {
// *              ……
// *          }
// *     }
// *
// *
// * </pre>
// */
//public class HttpRpcServer {
//    private static final Logger logger = LoggerFactory.getLogger(HttpRpcServer.class);
//
//    // ---------------------- build ----------------------
//
//    /**
//     * build http-rpc server
//     *
//     * @return HttpRpcServer
//     */
//    public static HttpRpcServer create() {
//        return new HttpRpcServer();
//    }
//
//    // ---------------------- field ----------------------
//
//    /**
//     * auth token
//     */
//    private String auth;
//
//    public HttpRpcServer auth(String auth) {
//        this.auth = auth;
//        return this;
//    }
//
//    /**
//     * service store
//     */
//    private final Map<String, Object> serviceStore = new ConcurrentHashMap<>();
//
//    /**
//     * register service
//     *
//     * @param serviceInstance service instance
//     */
//    public void register(Object serviceInstance) {
//        register(null, serviceInstance);
//    }
//
//    /**
//     * register service
//     *
//     * @param service            service name
//     * @param serviceInstance    service instance
//     */
//    public void register(String service, Object serviceInstance) {
//        // valid
//        if (serviceInstance == null) {
//            return;
//        }
//
//        // parse data
//        Class<?> serviceInterface = serviceInstance.getClass();
//        HttpRpcClient.HttpRpcService httpRpcService = serviceInterface.getAnnotation(HttpRpcClient.HttpRpcService.class);
//
//        // parse servicePath: api > service-annotation > service-class#getSimpleName
//        String servicePathFinal = HttpRpcClient.parseServicePath(service, httpRpcService, serviceInterface);
//
//        // set
//        serviceStore.put(servicePathFinal, serviceInstance);
//        logger.info("http-rpc server register success, service:{}, serviceInstance:{}", servicePathFinal, serviceInstance.getClass());
//    }
//
//    // ---------------------- invoke ----------------------
//
//    /**
//     * invoke
//     *
//     * @param servicName    service name
//     * @param methodName    method name
//     * @param data          method param data
//     * @return response, serialize to json
//     */
//    public String invoke(String servicName, String methodName, String data, String auth) throws Exception {
//        Object response = invokeWithResult(servicName, methodName, data, auth);
//        return GsonTool.toJson(response);
//    }
//
//    /**
//     * invoke (with origin object)
//     *
//     * @param servicName    service name
//     * @param methodName    method name
//     * @param data          method param data
//     * @return response, origin object
//     */
//    public Object invokeWithResult(String servicName, String methodName, String data, String auth) throws Exception {
//
//        // valid auth
//        if (StringTool.isNotBlank(this.auth)) {
//            if (StringTool.isBlank(auth)) {
//                throw new RuntimeException("http-rpc server invoke fail, auth not match[1]");
//            }
//            if (!this.auth.equals(auth)) {
//                throw new RuntimeException("http-rpc server invoke fail, auth not match[2]");
//            }
//        }
//
//        // match service
//        Object serviceInstance = serviceStore.get(servicName);
//        if (serviceInstance == null)  {
//            throw new RuntimeException("http-rpc server invoke fail, serviceInstance not found for: " + servicName);
//        }
//
//        // match method
//        Method method = null;
//        for (Method methodItem : serviceInstance.getClass().getMethods()) {
//            HttpRpcClient.HttpRpcMethod httpRpcMethod = methodItem.getAnnotation(HttpRpcClient.HttpRpcMethod.class);
//            // parse methodPath: method-annotation > method-name
//            String methodPath = HttpRpcClient.parseMethodPath(httpRpcMethod, methodItem);
//
//            // match method
//            if (methodPath.equals(methodName)) {
//                method = methodItem;
//            }
//        }
//        // match method with iface
//        if (method == null) {
//            for(Class<?> iface:serviceInstance.getClass().getInterfaces()) {
//                for (Method methodItem:iface.getMethods()) {
//                    HttpRpcClient.HttpRpcMethod httpRpcMethod = methodItem.getAnnotation(HttpRpcClient.HttpRpcMethod.class);
//                    // parse methodPath: method-annotation > method-name
//                    String methodPath = HttpRpcClient.parseMethodPath(httpRpcMethod, methodItem);
//
//                    // match method
//                    if (methodPath.equals(methodName)) {
//                        method = methodItem;
//                    }
//                }
//            }
//        }
//        if (method == null) {
//            throw new RuntimeException("http-rpc server invoke fail, method not found for: " + methodName);
//        }
//
//        // do invoke
//        if (method.getParameterTypes().length == 0) {
//            return method.invoke(serviceInstance);
//        } else if (method.getParameterTypes().length==1) {
//            // parse request
//            Object request = GsonTool.fromJson(data, method.getParameterTypes()[0]);
//
//            // invoke
//            return method.invoke(serviceInstance, request);
//        } else {    // if (method.getParameterTypes().length > 1)
//
//            // parse request
//            Object[] requests = new Object[method.getParameterTypes().length];
//            List<JsonElement> jsonElement = GsonTool.fromJsonList(data, JsonElement.class);
//
//            // invoke
//            for (int i = 0; i < method.getParameterTypes().length; i++) {
//                if (jsonElement.get(i) !=null ) {
//                    requests[i] = GsonTool.fromJsonElement(jsonElement.get(i), method.getParameterTypes()[i]);
//                } else {
//                    requests[i] = null;
//                }
//            }
//            return method.invoke(serviceInstance, requests);
//        }
//
//    }
//
//}