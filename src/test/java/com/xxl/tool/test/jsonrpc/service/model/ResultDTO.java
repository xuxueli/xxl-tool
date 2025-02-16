package com.xxl.tool.test.jsonrpc.service.model;

public class ResultDTO {

    private Boolean success;

    private String message;

    public ResultDTO() {
    }
    public ResultDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResultDTO{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
