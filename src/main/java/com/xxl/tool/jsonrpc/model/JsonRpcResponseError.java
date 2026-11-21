package com.xxl.tool.jsonrpc.model;

import java.io.Serializable;

/**
 * JSON-RPC 响应错误模型
 *
 * <p>以错误码与错误信息描述失败原因；错误码常量见本类。</p>
 *
 * <p>普通数据对象，非线程安全。</p>
 *
 * @author xuxueli
 */
public class JsonRpcResponseError implements Serializable {
    public static final long serialVersionUID = 42L;

    // ---------------------- error code ----------------------

    /**
     * 系统错误（服务端调用异常等未分类错误）
     */
    public static final int SYSTEM_ERROR = 3000;

    /**
     * 服务未找到
     */
    public static final int SERVICE_NOT_FOUND = 3001;

    /**
     * 方法未找到
     */
    public static final int METHOD_NOT_FOUND = 3002;

    /**
     * 请求参数错误（参数个数不匹配等）
     */
    public static final int REQUEST_PARAM_ERROR = 3003;

    // ---------------------- field ----------------------

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 创建空错误对象（供反序列化使用）
     */
    public JsonRpcResponseError() {
    }

    /**
     * 创建错误对象
     *
     * @param code   错误码
     * @param msg    错误信息
     */
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
