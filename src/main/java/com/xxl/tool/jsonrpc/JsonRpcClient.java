package com.xxl.tool.jsonrpc;

import com.google.gson.JsonElement;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.http.HttpTool;
import com.xxl.tool.jsonrpc.model.JsonRpcRequest;
import com.xxl.tool.jsonrpc.model.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON-RPC 客户端
 *
 * <p>通过 HTTP 调用 JSON-RPC 服务：既可直接调用 {@link #invoke} 传入服务名、方法名与参数，
 * 也可通过 {@link #proxy} 为服务接口生成动态代理，以本地接口方法的方式发起远程调用。</p>
 *
 * <p>构建方式为链式配置：{@code JsonRpcClient.newClient().url(...).timeout(...)}。
 * 客户端持有 URL、超时和请求头等可变配置，<b>非线程安全</b>；应在配置完成后使用，
 * 多线程场景下建议每个线程使用独立实例，或只读共享已配置完成的实例。</p>
 *
 * <p>依赖 Gson（JSON 序列化，provided）与 HTTP 工具（{@link HttpTool}）。</p>
 *
 * @author xuxueli
 */
public class JsonRpcClient {
    private static Logger logger = LoggerFactory.getLogger(JsonRpcClient.class);

    // ---------------------- build ----------------------

    /**
     * 创建客户端实例
     *
     * <p>初始未设置 URL，超时默认 3000 毫秒。</p>
     *
     * @return 新的客户端实例
     */
    public static JsonRpcClient newClient() {
        return new JsonRpcClient();
    }

    // ---------------------- field ----------------------

    private String url;
    private int timeout = 3000;     // by milliseconds
    private Map<String, String> headers = null;

    /**
     * 设置服务端 URL
     *
     * @param url   服务端地址，不可为 {@code null}
     * @return      当前实例，便于链式调用
     */
    public JsonRpcClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置连接与读取超时
     *
     * @param timeout   超时时间，单位毫秒
     * @return          当前实例，便于链式调用
     */
    public JsonRpcClient timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置全部请求头（直接替换当前引用，不做拷贝）
     *
     * @param headers   请求头 Map，可为 {@code null}
     * @return          当前实例，便于链式调用
     */
    public JsonRpcClient header(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 设置单个请求头
     *
     * <p>{@code key} 为空白或 {@code value} 为 {@code null} 时直接忽略；
     * 首次设置时自动初始化请求头容器。</p>
     *
     * @param key     请求头名称
     * @param value   请求头值
     * @return        当前实例，便于链式调用
     */
    public JsonRpcClient header(String key, String value) {
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

    // ---------------------- proxy ----------------------

    /**
     * 为服务接口创建代理，服务名默认使用接口全限定名
     *
     * @param serviceInterface   服务接口，不可为 {@code null}
     * @return                   服务代理实例
     * @param <T>                服务接口类型
     */
    public <T> T proxy(Class<T> serviceInterface) {
        return proxy(null, serviceInterface);
    }

    /**
     * 为服务接口创建代理
     *
     * <p>代理方法调用会转换为一次 JSON-RPC 请求；{@code service} 为 {@code null} 时
     * 使用接口全限定名作为服务名。所有调用异常（网络、序列化、服务端返回错误等）
     * 均以 {@link RuntimeException} 抛出。</p>
     *
     * @param service             服务名，可为 {@code null}
     * @param serviceInterface    服务接口，不可为 {@code null}
     * @return                    服务代理实例
     * @param <T>                 服务接口类型
     */
    @SuppressWarnings("unchecked")
    public <T> T proxy(final String service, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                (proxy, method, args) -> {

                    // parse param
                    String serviceName = service!=null?service:serviceInterface.getName();
                    String methodName = method.getName();
                    Type responseType = method.getGenericReturnType();

                    // parse type
                    Class<T> typeOfResponse = null;
                    Type[] typeArguments = null;
                    if (responseType == void.class) {                           // void
                        typeOfResponse = null;
                    } else if (responseType instanceof ParameterizedType) {     // 泛型
                        // parse type
                        typeOfResponse = (Class<T>) ((ParameterizedType)responseType).getRawType();
                        typeArguments = ((ParameterizedType)responseType).getActualTypeArguments();
                    } else {
                        typeOfResponse = (Class<T>) responseType;
                    }

                    // do invoke
                    return invoke(serviceName, methodName, args, typeOfResponse, typeArguments);
                });

    }

    /**
     * 发起调用（无泛型返回值）
     *
     * @param service       服务名
     * @param method        方法名
     * @param params        方法参数，可为 {@code null} 或空数组
     * @param responseType  返回值类型；为 {@code null} 表示无返回值（void）
     * @return              反序列化后的返回值，无返回值时为 {@code null}
     * @param <T>           返回值类型
     * @throws RuntimeException 请求失败、响应为空、服务端返回错误或反序列化失败时抛出
     */
    public <T> T invoke(String service,
                        String method,
                        Object[] params,
                        Class<T> responseType) {
        return invoke(service, method, params, responseType, null);
    }

    /**
     * 发起调用（支持泛型返回值）
     *
     * <p>请求参数先逐个序列化为 JSON，再发送 HTTP POST 请求；响应中的错误码大于 0 时视为失败。
     * 方法内部捕获全部异常并统一包装为 {@link RuntimeException} 抛出。</p>
     *
     * @param service       服务名
     * @param method        方法名
     * @param params        方法参数，可为 {@code null} 或空数组
     * @param responseType  返回值原始类型；为 {@code null} 表示无返回值（void）
     * @param typeArguments 泛型实参类型，可为 {@code null}；如返回 {@code Response<UserDTO>} 时传 {@code UserDTO.class}
     * @return              反序列化后的返回值，无返回值时为 {@code null}
     * @param <T>           返回值类型
     * @throws RuntimeException 请求失败、响应为空、服务端返回错误或反序列化失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String service,
                        String method,
                        Object[] params,
                        Class<T> responseType,
                        Type[] typeArguments) {

        try {
            // 1、params 2 request
            JsonElement[] paramJsons = null;
            if (params != null) {
                paramJsons = new JsonElement[params.length];
                for (int i = 0; i < params.length; i++) {
                    paramJsons[i] = GsonTool.toJsonElement(params[i]);
                }
            }
            JsonRpcRequest request = new JsonRpcRequest(service, method, paramJsons);

            // 2、do request
            /**
             * remoting
             *
             * Send：
             *      1、client serilize：Request -> json
             *      2、http remoting
             *      3、server deserilize：json -> Request
             * Receive：
             *      1、server serilize: Response -> json
             *      2、http remoting
             *      3、client deserilize：json -> Response
             *
             */
            String requestJson = GsonTool.toJson(request);
            String responseData = HttpTool
                    .createPost(url)
                    .connectTimeout(timeout)
                    .readTimeout(timeout)
                    .header(headers)
                    .body(requestJson)
                    .execute()
                    .response();

            // 3、parse response
            if (responseData.isEmpty()) {
                throw new RuntimeException("response data not found");
            }
            JsonRpcResponse response = GsonTool.fromJson(responseData, JsonRpcResponse.class);
            if (response.isError()) {
                throw new RuntimeException("invoke error: " + response.getError());
            }

            // 4、response 2 result
            if (responseType==null) {                        // void
                return null;
            } else if (typeArguments != null) {         // 泛型
                return GsonTool.fromJsonElement(response.getResult(), responseType, typeArguments);
            } else {
                return GsonTool.fromJsonElement(response.getResult(), responseType);
            }
        } catch (Throwable e) {
            throw new RuntimeException("invoke error[2], service:"+service+", method:" + method, e);
        }
    }

}
