package com.xxl.tool.response;

import java.io.Serializable;


/**
 * Response (from ReturnT)
 *
 * @author xuxueli 2015-12-4 16:32:31
 * @param <T>
 */
public class Response <T> implements Serializable {
    public static final long serialVersionUID = 42L;

    private int code;

    private String msg;

    private T data;

    public Response() {}
    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public boolean isSuccess() {
        return code == ResponseCode.CODE_200.getCode();
    }

}
