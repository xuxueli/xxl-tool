package com.xxl.tool.jsonrpc;

import com.google.gson.JsonElement;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.jsonrpc.model.JsonRpcRequest;
import com.xxl.tool.jsonrpc.model.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * json-rpc server
 *
 * @author xuxueli
 */
public class JsonRpcServer {
    private static Logger logger = LoggerFactory.getLogger(JsonRpcServer.class);

    public JsonRpcServer() {
    }
    public JsonRpcServer(Map<String, Object> initServiceStore) {
        // init service store
        if (initServiceStore!=null && !initServiceStore.isEmpty()) {
            for (Map.Entry<String, Object> entry: initServiceStore.entrySet()) {
                register(entry.getKey(), entry.getValue());
            }
        }
    }

    // --------------------------------- service store ---------------------------------
    /**
     * service store
     */
    private Map<String, Object> serviceStore = new ConcurrentHashMap<>();

    /**
     * register service
     * @param service
     * @param serviceInstance
     */
    public void register(String service, Object serviceInstance) {
        serviceStore.put(service, serviceInstance);
    }

    /**
     * invoke (with origin data)
     *
     * @param requestBody
     * @return
     */
    public String invoke(String requestBody) {
        // request json 2 object
        JsonRpcRequest jsonRpcRequest = GsonTool.fromJson(requestBody, JsonRpcRequest.class);

        // invoke
        JsonRpcResponse jsonRpcResponse = invoke(jsonRpcRequest);

        // response object 2 json
        return GsonTool.toJson(jsonRpcResponse);
    }

    /**
     * invoke (with object)
     *
     * @param request
     * @return
     */
    public JsonRpcResponse invoke(JsonRpcRequest request) {

        // parse request
        String service = request.getService();
        String method = request.getMethod();
        JsonElement[] params = request.getParams();

        try {
            // 1、match service  ： url 匹配
            Object serviceInstance = serviceStore.get(service);
            if (serviceInstance == null) {
                return JsonRpcResponse.ofError("service[" + service + "] not found.");
            }

            // 2、match method： 不允许同名方法，否则随机第一个
            Method methodObj = null;
            for (Method m : serviceInstance.getClass().getMethods()) {
                if (m.getName().equals(method)) {
                    methodObj = m;
                    break;
                }
            }
            if (methodObj == null) {
                return JsonRpcResponse.ofError("method [" + method + "] not found.");
            }

            // 3、param
            Object[] parameters = null;
            Class<?>[] parameterTypes = methodObj.getParameterTypes();
            if (parameterTypes.length > 0) {
                // valid
                if (params == null || params.length != parameterTypes.length) {
                    return JsonRpcResponse.ofError("method[" + method + "] params number not match.");
                }

                // fill param
                parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = GsonTool.fromJsonElement(params[i], parameterTypes[i]);
                }
            }

            // 4、invoke
            methodObj.setAccessible(true);
            Object resultObj = methodObj.invoke(serviceInstance, parameters);
            JsonElement resultJson = GsonTool.toJsonElement(resultObj);

            return JsonRpcResponse.ofSuccess(resultJson);
        } catch (Exception e) {
            logger.error("server invoke error:{}", e.getMessage(), e);
            return JsonRpcResponse.ofError("server invoke error: " + e.getMessage());       // todo1, log may missing
        }

    }

}
