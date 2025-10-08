package com.xxl.tool.jsonrpc.model;

import java.io.Serializable;

/**
 * response error for json-rpc
 *
 * @author xuxueli
 */
public class JsonRpcResponseError implements Serializable {
    public static final long serialVersionUID = 42L;

    // ---------------------- error code ----------------------

    /**
     * system error
     */
    public static final int SYSTEM_ERROR = 3000;

    /**
     * service not found
     */
    public static final int SERVICE_NOT_FOUND = 3001;

    /**
     * method not found
     */
    public static final int METHOD_NOT_FOUND = 3002;

    /**
     * request param error
     */
    public static final int REQUEST_PARAM_ERROR = 3003;

    // ---------------------- field ----------------------

    private int code;

    private String msg;

    public JsonRpcResponseError() {
    }
    public JsonRpcResponseError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "JsonRpcResponseError{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

}
