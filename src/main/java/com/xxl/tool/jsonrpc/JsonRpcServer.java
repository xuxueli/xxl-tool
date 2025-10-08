package com.xxl.tool.jsonrpc;

import com.google.gson.JsonElement;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.jsonrpc.model.JsonRpcRequest;
import com.xxl.tool.jsonrpc.model.JsonRpcResponse;
import com.xxl.tool.jsonrpc.model.JsonRpcResponseError;
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
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcServer.class);

    // --------------------------------- build ---------------------------------

    public static JsonRpcServer newServer() {
        return new JsonRpcServer();
    }

    // --------------------------------- field ---------------------------------

    /**
     * service store
     */
    private final Map<String, Object> serviceStore = new ConcurrentHashMap<>();

    /**
     * register service
     * @param service               the service name
     * @param serviceInstance       the service instance
     */
    public JsonRpcServer register(String service, Object serviceInstance) {
        serviceStore.put(service, serviceInstance);
        return this;
    }

    /**
     * register service
     *
     * @param initServiceStore      the service store
     */
    public JsonRpcServer register(Map<String, Object> initServiceStore) {
        if (MapTool.isEmpty(initServiceStore)) {
            return this;
        }

        // init service store
        for (Map.Entry<String, Object> entry: initServiceStore.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
        return this;
    }


    // --------------------------------- invoke ---------------------------------

    /**
     * invoke (with origin data)
     *
     * @param requestBody   the request body
     * @return              the response body
     */
    public String invoke(String requestBody) {
        // request json 2 object
        JsonRpcRequest jsonRpcRequest = GsonTool.fromJson(requestBody, JsonRpcRequest.class);

        // invoke
        JsonRpcResponse jsonRpcResponse = doInvoke(jsonRpcRequest);

        // response object 2 json
        return GsonTool.toJson(jsonRpcResponse);
    }

    /**
     * invoke (with object)
     *
     * @param request       the  request
     * @return              the  response
     */
    private JsonRpcResponse doInvoke(JsonRpcRequest request) {

        // parse request
        String service = request.getService();
        String method = request.getMethod();
        JsonElement[] params = request.getParams();

        try {
            // 1、match service  ： url 匹配
            Object serviceInstance = serviceStore.get(service);
            if (serviceInstance == null) {
                return JsonRpcResponse.ofError(JsonRpcResponseError.SERVICE_NOT_FOUND, "service[" + service + "] not found.");
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
                return JsonRpcResponse.ofError(JsonRpcResponseError.METHOD_NOT_FOUND, "method [" + method + "] not found.");
            }

            // 3、param array
            Object[] parameters = null;
            Class<?>[] parameterTypes = methodObj.getParameterTypes();
            if (parameterTypes.length > 0) {
                // valid
                if (params == null || params.length != parameterTypes.length) {
                    return JsonRpcResponse.ofError(JsonRpcResponseError.REQUEST_PARAM_ERROR,"method[" + method + "] params number not match.");
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
