package com.xxl.tool.emoji.exception;

/**
 * Emoji 模块异常
 *
 * <p>用于 Emoji 数据加载失败、Emoji 能力使用不当等场景，继承 {@link RuntimeException}，
 * 不强制调用方捕获。</p>
 *
 * @author xuxueli 2018-07-04 21:01:11
 */
public class XxlEmojiException extends RuntimeException {

    /**
     * 创建无异常信息的异常
     */
    public XxlEmojiException() {
    }

    /**
     * 创建带异常信息的异常
     *
     * @param message   异常描述
     */
    public XxlEmojiException(String message) {
        super(message);
    }

}
