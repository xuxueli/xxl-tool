package com.xxl.tool.error;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具：将异常转换为可打印的堆栈字符串
 *
 * <p>
 * 仅用于日志、诊断输出；返回值可能包含调用栈中的参数与内部信息，不要直接返回给不可信调用方。
 * 本类无状态，线程安全。
 * </p>
 *
 * @author xuxueli 2018-10-20 20:07:26
 */
public class ThrowableTool {

    /**
     * parse error to string
     *
     * <p>
     * 等价于 {@code e.printStackTrace(new PrintWriter(StringWriter))}，返回完整堆栈文本。
     * </p>
     *
     * @param e  error，不能为 {@code null}
     * @return 包含异常类型、消息与堆栈的字符串
     * @throws NullPointerException 当 {@code e} 为 {@code null} 时
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
