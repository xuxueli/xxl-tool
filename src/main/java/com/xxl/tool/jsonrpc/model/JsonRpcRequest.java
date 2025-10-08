package com.xxl.tool.jsonrpc.model;

import com.google.gson.JsonElement;

import java.io.Serializable;
import java.util.Arrays;

/**
 * request for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcRequest implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * service name
     */
    String service;

    /**
     * method name
     */
    String method;

    /**
     * method params
     */
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

    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }

}