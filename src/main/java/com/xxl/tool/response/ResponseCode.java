package com.xxl.tool.response;

/**
 * response code
 *
 * @author xuxueli 2015-12-4
 */
public enum ResponseCode {

    CODE_200(200, "成功"),

    CODE_201(201, "未知错误"),
    CODE_202(202, "业务异常"),
    CODE_203(203, "系统异常");

    private final int code;
    private final String msg;

    private ResponseCode(int code, String msg) {
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
