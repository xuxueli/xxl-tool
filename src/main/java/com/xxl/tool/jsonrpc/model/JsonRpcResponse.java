package com.xxl.tool.jsonrpc.model;

/**
 * response for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcResponse {

    String error;
    String data;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // tool
    public static JsonRpcResponse ofSuccess(String data) {
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