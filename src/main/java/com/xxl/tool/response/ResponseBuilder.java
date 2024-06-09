package com.xxl.tool.response;

/**
 * response builder
 *
 * @author xuxueli 2015-12-4
 */
public class ResponseBuilder {

    private Response response;

    public ResponseBuilder() {
        this.response = new Response();
    }

    public ResponseBuilder code(int code) {
        response.setCode(code);
        return this;
    }
    public ResponseBuilder msg(String msg) {
        response.setMsg(msg);
        return this;
    }
    public ResponseBuilder data(Object data) {
        response.setData(data);
        return this;
    }

    public Response build() {
        return response;
    }


    public static ResponseBuilder newBuilder(){
        return new ResponseBuilder();
    }
    public static ResponseBuilder newSuccessBuilder(){
        return new ResponseBuilder()
                .code(ResponseCode.CODE_200.getCode())
                .msg(ResponseCode.CODE_200.getMsg());
    }
    public static ResponseBuilder newFailBuilder(){
        return new ResponseBuilder()
                .code(ResponseCode.CODE_203.getCode())
                .msg(ResponseCode.CODE_203.getMsg());
    }

}
