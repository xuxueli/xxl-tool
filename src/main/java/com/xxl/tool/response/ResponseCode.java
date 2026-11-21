package com.xxl.tool.response;

/**
 * 统一响应状态码
 *
 * <p>约定：成功为 200，通用失败为 500；4xx 表示客户端参数/认证问题，5xx 表示服务端问题。
 * {@link #CODE_500} 与 {@link #FAILURE} 的 code 相同（均为 500），属于别名，按 code 比较时等价。</p>
 *
 * <p>枚举不可变，线程安全。</p>
 *
 * @author xuxueli 2015-12-4
 */
public enum ResponseCode {

    // common
    /** 成功，code=200 */
    SUCCESS(200, "Success"),
    /** 通用失败，code=500 */
    FAILURE(500, "Fail"),

    // client fail
    /** 参数非法，code=400 */
    CODE_400(400, "Invalid Argument"),
    /** 未认证，code=401 */
    CODE_401(401, "Not Authorized"),

    // server fail
    /** 服务端失败，code=500，与 {@link #FAILURE} 别名 */
    CODE_500(FAILURE.code, FAILURE.getMsg()),
    /** 服务端错误，code=501 */
    CODE_501(501, "Server Error"),
    /** 服务不可用，code=502 */
    CODE_502(502, "Server Unavailable");

    private final int code;
    private final String msg;

    /**
     * 构造状态码
     *
     * @param code 状态码
     * @param msg  默认提示信息
     */
    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取状态码
     *
     * @return 状态码数值
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取默认提示信息
     *
     * @return 提示信息
     */
    public String getMsg() {
        return msg;
    }

}
