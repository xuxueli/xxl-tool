package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

import java.io.Serializable;

/**
 * JSON-RPC 响应模型
 *
 * <p>响应要么携带结果 {@link #result}，要么携带错误 {@link #error}；通过
 * {@link #isSuccess()} / {@link #isError()} 判断。提供成功与错误的静态构造方法。</p>
 *
 * <p>普通数据对象，非线程安全。</p>
 *
 * @author xuxueli
 */
public class JsonRpcResponse implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * 错误信息，成功时为 {@code null}
     */
    JsonRpcResponseError error;

    /**
     * 结果数据，失败时为 {@code null}
     */
    JsonElement result;

    /**
     * 获取错误信息
     *
     * @return 错误信息，成功时为 {@code null}
     */
    public JsonRpcResponseError getError() {
        return error;
    }

    /**
     * 设置错误信息
     *
     * @param error   错误信息
     */
    public void setError(JsonRpcResponseError error) {
        this.error = error;
    }

    /**
     * 获取结果数据
     *
     * @return 结果数据，失败时为 {@code null}
     */
    public JsonElement getResult() {
        return result;
    }

    /**
     * 设置结果数据
     *
     * @param result   结果数据
     */
    public void setResult(JsonElement result) {
        this.result = result;
    }

    /**
     * 返回响应的字符串表示，包含错误信息与结果
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "error=" + error +
                ", result=" + result +
                '}';
    }

    // tool
    /**
     * 是否成功（无错误信息）
     *
     * @return 成功返回 {@code true}
     */
    public boolean isSuccess() {
        return !isError();
    }

    /**
     * 是否失败
     *
     * <p>存在错误信息且错误码大于 0 时视为失败。</p>
     *
     * @return 失败返回 {@code true}
     */
    public boolean isError() {
        return error!=null && error.getCode()>0;
    }

    // build
    /**
     * 构造成功响应
     *
     * @param result   结果数据，可为 {@code null}
     * @return         成功响应
     */
    public static JsonRpcResponse ofSuccess(JsonElement result) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setResult(result);
        return response;
    }

    /**
     * 构造系统错误响应
     *
     * @param error   错误描述
     * @return        错误码为 {@link JsonRpcResponseError#SYSTEM_ERROR} 的响应
     */
    public static JsonRpcResponse ofError(String error) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(new JsonRpcResponseError(JsonRpcResponseError.SYSTEM_ERROR, error));
        return response;
    }

    /**
     * 构造指定错误码的响应
     *
     * @param errorCode   错误码
     * @param errorMsg    错误描述
     * @return            错误响应
     */
    public static JsonRpcResponse ofError(int errorCode, String errorMsg) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(new JsonRpcResponseError(errorCode, errorMsg));
        return response;
    }

}