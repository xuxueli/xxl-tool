package com.xxl.tool.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xuxueli 2018-10-20 20:07:26
 */
public class ThrowableTool {

    /**
     * parse error to string
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
