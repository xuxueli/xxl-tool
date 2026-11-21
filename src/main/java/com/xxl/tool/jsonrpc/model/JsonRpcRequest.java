package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

import java.io.Serializable;
import java.util.Arrays;

/**
 * JSON-RPC 请求模型
 *
 * <p>描述一次远程调用：服务名、方法名和参数数组。参数以 {@link JsonElement} 保存，
 * 由服务端按目标方法参数类型反序列化。字段通过 getter/setter 访问，便于 Gson 序列化与反序列化。</p>
 *
 * <p>普通数据对象，非线程安全。</p>
 *
 * @author xuxueli
 */
public class JsonRpcRequest implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * 服务名
     */
    String service;

    /**
     * 方法名
     */
    String method;

    /**
     * 方法参数
     */
    JsonElement[] params;

    /**
     * 创建空请求（供反序列化使用）
     */
    public JsonRpcRequest() {
    }

    /**
     * 创建请求
     *
     * @param service   服务名
     * @param method    方法名
     * @param params    方法参数，可为 {@code null}
     */
    public JsonRpcRequest(String service, String method, JsonElement[] params) {
        this.service = service;
        this.method = method;
        this.params = params;
    }

    /**
     * 获取服务名
     *
     * @return 服务名
     */
    public String getService() {
        return service;
    }

    /**
     * 设置服务名
     *
     * @param service   服务名
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * 获取方法名
     *
     * @return 方法名
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置方法名
     *
     * @param method   方法名
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 获取方法参数
     *
     * @return 参数数组，可能为 {@code null}
     */
    public JsonElement[] getParams() {
        return params;
    }

    /**
     * 设置方法参数
     *
     * @param params   参数数组，可为 {@code null}
     */
    public void setParams(JsonElement[] params) {
        this.params = params;
    }

    /**
     * 返回请求的字符串表示，包含服务名、方法名与参数
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }

}