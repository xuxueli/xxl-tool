package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

import java.io.Serializable;

/**
 * response for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcResponse implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * error data
     */
    JsonRpcResponseError error;

    /**
     * result data
     */
    JsonElement result;

    public JsonRpcResponseError getError() {
        return error;
    }

    public void setError(JsonRpcResponseError error) {
        this.error = error;
    }

    public JsonElement getResult() {
        return result;
    }

    public void setResult(JsonElement result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "error=" + error +
                ", result=" + result +
                '}';
    }

    // tool
    /**
     * is success
     */
    public boolean isSuccess() {
        return !isError();
    }

    /**
     * is error
     */
    public boolean isError() {
        return error!=null && error.getCode()>0;
    }

    // build
    public static JsonRpcResponse ofSuccess(JsonElement result) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setResult(result);
        return response;
    }

    public static JsonRpcResponse ofError(String error) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(new JsonRpcResponseError(JsonRpcResponseError.SYSTEM_ERROR, error));
        return response;
    }

    public static JsonRpcResponse ofError(int errorCode, String errorMsg) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(new JsonRpcResponseError(errorCode, errorMsg));
        return response;
    }

}