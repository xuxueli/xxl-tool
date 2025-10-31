package com.xxl.tool.response;

import java.io.Serializable;


/**
 * Response (from ReturnT of xxl-job)
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


    // --------------------------- tool ---------------------------

    /**
     * is success
     *
     * @return true if success, false otherwise
     */
    public boolean isSuccess() {
        return code == ResponseCode.SUCCESS.getCode();
    }

    /**
     * is success
     *
     * @param response the response
     * @return true if success, false otherwise
     */
    public static boolean isSuccess(Response<?> response) {
        return response!=null && response.isSuccess();
    }


    // --------------------------- build ---------------------------

    /**
     * build response
     */
    public static <T> Response<T> of(int code, String msg, T data) {
        return new Response<T>(code, msg, data);
    }

    /**
     * build response
     */
    public static <T> Response<T> of(int code, String msg) {
        return new Response<T>(code, msg, null);
    }

    /**
     * build success response
     */
    public static <T> Response<T> ofSuccess(T data) {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), data);
    }

    /**
     * build success response
     */
    public static <T> Response<T> ofSuccess() {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), null);
    }

    /**
     * build fail response
     */
    public static <T> Response<T> ofFail(String msg) {
        return new Response<T>(ResponseCode.FAILURE.getCode(), msg, null);
    }

    /**
     * build fail response
     */
    public static <T> Response<T> ofFail() {
        return new Response<T>(ResponseCode.FAILURE.getCode(), ResponseCode.FAILURE.getMsg(), null);
    }

}
