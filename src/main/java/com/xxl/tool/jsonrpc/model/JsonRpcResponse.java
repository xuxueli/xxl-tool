package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

/**
 * response for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcResponse {

    String error;
    JsonElement data;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    // tool
    public static JsonRpcResponse ofSuccess(JsonElement data) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setData(data);
        return response;
    }

    public static JsonRpcResponse ofError(String error) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(error);
        return response;
    }

}