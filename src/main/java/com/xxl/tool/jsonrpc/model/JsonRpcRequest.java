package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

/**
 * request for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcRequest {

    String service;
    String method;
    JsonElement[] params;

    public JsonRpcRequest() {
    }
    public JsonRpcRequest(String service, String method, JsonElement[] params) {
        this.service = service;
        this.method = method;
        this.params = params;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonElement[] getParams() {
        return params;
    }

    public void setParams(JsonElement[] params) {
        this.params = params;
    }

}