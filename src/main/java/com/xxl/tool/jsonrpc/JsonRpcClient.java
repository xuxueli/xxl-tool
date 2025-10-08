package com.xxl.tool.jsonrpc;

import com.google.gson.JsonElement;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.http.HttpTool1;
import com.xxl.tool.jsonrpc.model.JsonRpcRequest;
import com.xxl.tool.jsonrpc.model.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * json-rpc client
 *
 * @author xuxueli
 */
public class JsonRpcClient {
    private static Logger logger = LoggerFactory.getLogger(JsonRpcClient.class);

    private String url;
    private int timeout = 3000;     // by milliseconds
    private Map<String, String> headers = null;

    public JsonRpcClient(String url) {
        this.url = url;
    }

    public JsonRpcClient(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    public JsonRpcClient(String url, int timeout, Map<String, String> headers) {
        this.url = url;
        this.timeout = timeout;
        this.headers = headers;
    }

    public <T> T proxy(Class<T> serviceInterface) {
        return proxy(null, serviceInterface);
    }

    /**
     * proxy service
     * @param service               the service name
     * @param serviceInterface      the service interface
     * @return  service
     * @param <T> the service interface
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
     * invoke with params
     *
     * @param service               the service name
     * @param method                the method name
     * @param params                the method params
     * @param responseType          the response type
     * @return  response            the  response
     * @param <T> the response type
     */
    public <T> T invoke(String service,
                        String method,
                        Object[] params,
                        Class<T> responseType) {
        return invoke(service, method, params, responseType, null);
    }

    /**
     * invoke with params
     *
     * @param service               the service name
     * @param method                the method name
     * @param params                the method params
     * @param responseType          the response type
     * @param typeArguments         the response type arguments
     * @return  response            the  response
     * @param <T> the response type
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
            String responseData = HttpTool1.postBody(url, requestJson, headers, timeout);

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
