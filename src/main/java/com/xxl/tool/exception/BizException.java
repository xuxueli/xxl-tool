package com.xxl.tool.exception;

/**
 * @author xuxueli 2024-06-30
 */
public class BizException extends RuntimeException {

    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

}