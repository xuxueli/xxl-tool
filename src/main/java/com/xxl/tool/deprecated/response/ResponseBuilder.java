//package com.xxl.tool.response;
//
///**
// * response builder
// *
// * @author xuxueli 2015-12-4
// */
//public class ResponseBuilder<T> {
//
//    private final Response<T> response;
//
//    public ResponseBuilder() {
//        this.response = new Response<T>();
//    }
//
//    // fill data
//    public ResponseBuilder<T> code(int code) {
//        response.setCode(code);
//        return this;
//    }
//    public ResponseBuilder<T> msg(String msg) {
//        response.setMsg(msg);
//        return this;
//    }
//    public ResponseBuilder<T> data(T data) {
//        response.setData(data);
//        return this;
//    }
//
//    // fast fill data
//    public ResponseBuilder<T> success() {
//        response.setCode(ResponseCode.CODE_200.getCode());
//        response.setMsg(ResponseCode.CODE_200.getMsg());
//        return this;
//    }
//    public ResponseBuilder<T> success(T data) {
//        response.setCode(ResponseCode.CODE_200.getCode());
//        response.setMsg(ResponseCode.CODE_200.getMsg());
//        response.setData(data);
//        return this;
//    }
//    public ResponseBuilder<T> fail() {
//        response.setCode(ResponseCode.CODE_203.getCode());
//        response.setMsg(ResponseCode.CODE_203.getMsg());
//        return this;
//    }
//    public ResponseBuilder<T> fail(String msg) {
//        response.setCode(ResponseCode.CODE_203.getCode());
//        response.setMsg(msg);
//        return this;
//    }
//
//    // build
//    public Response<T> build() {
//        return response;
//    }
//
//}
