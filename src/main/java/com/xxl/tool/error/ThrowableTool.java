package com.xxl.tool.error;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xuxueli 2018-10-20 20:07:26
 */
public class ThrowableTool {

    /**
     * parse error to string
     *
     * @param e  error
     * @return error string
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
