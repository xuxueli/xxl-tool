package com.xxl.tool.jsonrpc;

import com.google.gson.JsonElement;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.jsonrpc.model.JsonRpcRequest;
import com.xxl.tool.jsonrpc.model.JsonRpcResponse;
import com.xxl.tool.jsonrpc.model.JsonRpcResponseError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-RPC 服务端
 *
 * <p>维护“服务名 → 服务实例”的注册表，接收 JSON-RPC 请求 JSON，按服务名与方法名反射调用
 * 本地实例方法，并把结果或错误封装为响应 JSON。仅负责协议层处理，不绑定具体 HTTP 容器；
 * 使用方可将 {@link #invoke(String)} 接入任意 HTTP 框架。</p>
 *
 * <p>服务注册表基于 {@link ConcurrentHashMap}，注册与调用可并发进行；同一服务名重复注册会覆盖旧实例。
 * 服务实例本身是否线程安全由使用方保证。</p>
 *
 * <p>依赖 Gson（provided）。</p>
 *
 * @author xuxueli
 */
public class JsonRpcServer {
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcServer.class);

    // --------------------------------- build ---------------------------------

    /**
     * 创建服务端实例
     *
     * @return 新的服务端实例，初始未注册任何服务
     */
    public static JsonRpcServer newServer() {
        return new JsonRpcServer();
    }

    // --------------------------------- field ---------------------------------

    /**
     * service store
     */
    private final Map<String, Object> serviceStore = new ConcurrentHashMap<>();

    /**
     * 注册单个服务
     *
     * <p>同名服务会被新实例覆盖。</p>
     *
     * @param service           服务名，不可为 {@code null}
     * @param serviceInstance   服务实例，不可为 {@code null}
     * @return                  当前实例，便于链式调用
     * @throws NullPointerException 服务名或实例为 {@code null} 时抛出
     */
    public JsonRpcServer register(String service, Object serviceInstance) {
        serviceStore.put(service, serviceInstance);
        return this;
    }

    /**
     * 批量注册服务
     *
     * <p>{@code initServiceStore} 为 {@code null} 或空 Map 时不做任何处理；同名服务会被覆盖。</p>
     *
     * @param initServiceStore   服务名到服务实例的映射，可为 {@code null}
     * @return                   当前实例，便于链式调用
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
     * 处理 JSON-RPC 请求
     *
     * <p>请求体反序列化为 {@link JsonRpcRequest} 后查找服务与方法并反射调用；
     * 服务不存在、方法不存在、参数个数不匹配、反射调用异常等业务错误均转换为带错误码的
     * {@link JsonRpcResponse}，不向调用方抛出。请求体为空或格式非法时，
     * 反序列化结果可能为 {@code null}，此时会抛出运行时异常。</p>
     *
     * @param requestBody   请求 JSON，不可为 {@code null}
     * @return              响应 JSON，不会返回 {@code null}
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
     * 执行请求并构造响应
     *
     * <p>方法按名称匹配，同名重载方法只取反射返回的第一个，因此服务接口不应存在同名方法。</p>
     *
     * @param request   已反序列化的请求
     * @return          响应对象，业务与框架错误均通过 error 字段表达
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
