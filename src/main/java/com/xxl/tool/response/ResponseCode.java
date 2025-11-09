package com.xxl.tool.response;

/**
 * response code
 *
 * @author xuxueli 2015-12-4
 */
public enum ResponseCode {

    // common
    SUCCESS(200, "Success"),
    FAILURE(500, "Fail"),

    // client fail
    CODE_400(400, "Invalid Argument"),
    CODE_401(401, "Not Authorized"),

    // server fali
    CODE_500(FAILURE.code, FAILURE.getMsg()),
    CODE_501(501, "Server Error"),
    CODE_502(502, "Server Unavailable");

    private final int code;
    private final String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
