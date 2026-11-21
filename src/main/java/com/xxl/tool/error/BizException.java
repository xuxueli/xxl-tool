package com.xxl.tool.error;

/**
 * 通用业务异常：用于表达可预期的业务失败，不绑定 Web 框架
 *
 * <p>
 * 继承 {@link RuntimeException}，调用方无需显式捕获；异常信息不应包含密码、Token 等敏感数据。
 * 异常实例本身不可变且线程安全。
 * </p>
 *
 * @author xuxueli 2024-06-30
 */
public class BizException extends RuntimeException {

    /**
     * 构造无异常信息的业务异常
     */
    public BizException() {
    }

    /**
     * 构造带异常信息的业务异常
     *
     * @param message 异常描述，应包含可定位的业务上下文，禁止写入敏感信息
     */
    public BizException(String message) {
        super(message);
    }

    /**
     * 构造由底层异常包装的业务异常
     *
     * @param cause 原始异常，可为 {@code null}
     */
    public BizException(Throwable cause) {
        super(cause);
    }

}