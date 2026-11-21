package com.xxl.tool.response;

import java.io.Serializable;


/**
 * 统一响应模型（源自 xxl-job 的 ReturnT）
 *
 * <p>结构固定为 {@code code + msg + data}：{@link #getCode()} 为业务状态码，
 * 成功使用 {@link ResponseCode#SUCCESS}（200），失败通常使用 {@link ResponseCode#FAILURE}（500）；
 * {@link #getMsg()} 为提示信息；{@link #getData()} 为业务数据，失败或无需返回数据时为 {@code null}。
 * 完整的 code 定义见 {@link ResponseCode}。</p>
 *
 * <p>可变 POJO，<b>非线程安全</b>；可序列化，{@code serialVersionUID = 42L}。
 * 仅作为数据载体，不绑定 Web 框架。</p>
 *
 * @author xuxueli 2015-12-4 16:32:31
 * @param <T> 业务数据类型
 */
public class Response <T> implements Serializable {
    public static final long serialVersionUID = 42L;

    /**
     * 业务状态码，参见 {@link ResponseCode}
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 空构造，供反序列化/框架使用
     */
    public Response() {}

    /**
     * 构造无数据的响应
     *
     * @param code 业务状态码
     * @param msg  提示信息
     */
    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 构造带数据的响应
     *
     * @param code 业务状态码
     * @param msg  提示信息
     * @param data 业务数据，可为 {@code null}
     */
    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取业务状态码
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置业务状态码
     *
     * @param code 业务状态码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取提示信息
     *
     * @return 提示信息，可能为 {@code null}
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置提示信息
     *
     * @param msg 提示信息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取业务数据
     *
     * @return 业务数据，可能为 {@code null}
     */
    public T getData() {
        return data;
    }

    /**
     * 设置业务数据
     *
     * @param data 业务数据，可为 {@code null}
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回响应字段的调试字符串
     *
     * @return 包含 code、msg、data 的字符串
     */
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
     * 判断当前响应是否成功
     *
     * @return {@code code == ResponseCode.SUCCESS.getCode()} 时返回 {@code true}
     */
    public boolean isSuccess() {
        return code == ResponseCode.SUCCESS.getCode();
    }

    /**
     * 判断响应是否成功（空安全）
     *
     * @param response 待判断响应，可为 {@code null}
     * @return 非空且 {@link #isSuccess()} 为 {@code true} 时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isSuccess(Response<?> response) {
        return response!=null && response.isSuccess();
    }


    // --------------------------- build ---------------------------

    /**
     * 构造指定状态码、信息与数据的响应
     *
     * @param code 业务状态码
     * @param msg  提示信息
     * @param data 业务数据，可为 {@code null}
     * @param <T>  业务数据类型
     * @return 新的响应对象
     */
    public static <T> Response<T> of(int code, String msg, T data) {
        return new Response<T>(code, msg, data);
    }

    /**
     * 构造无数据的响应
     *
     * @param code 业务状态码
     * @param msg  提示信息
     * @param <T>  业务数据类型
     * @return 新的响应对象，{@code data} 为 {@code null}
     */
    public static <T> Response<T> of(int code, String msg) {
        return new Response<T>(code, msg, null);
    }

    /**
     * 构造成功响应（携带数据）
     *
     * @param data 业务数据，可为 {@code null}
     * @param <T>  业务数据类型
     * @return code 为 {@link ResponseCode#SUCCESS} 的响应
     */
    public static <T> Response<T> ofSuccess(T data) {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), data);
    }

    /**
     * 构造成功响应（无数据）
     *
     * @param <T> 业务数据类型
     * @return code 为 {@link ResponseCode#SUCCESS}、{@code data} 为 {@code null} 的响应
     */
    public static <T> Response<T> ofSuccess() {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), null);
    }

    /**
     * 构造失败响应（自定义提示信息）
     *
     * @param msg 失败提示信息，为 {@code null} 时保留 {@code null}
     * @param <T> 业务数据类型
     * @return code 为 {@link ResponseCode#FAILURE} 的响应
     */
    public static <T> Response<T> ofFail(String msg) {
        return new Response<T>(ResponseCode.FAILURE.getCode(), msg, null);
    }

    /**
     * 构造失败响应（默认提示信息）
     *
     * @param <T> 业务数据类型
     * @return code 为 {@link ResponseCode#FAILURE}、msg 为默认值的响应
     */
    public static <T> Response<T> ofFail() {
        return new Response<T>(ResponseCode.FAILURE.getCode(), ResponseCode.FAILURE.getMsg(), null);
    }

}
