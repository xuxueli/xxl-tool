package com.xxl.tool.test.core;

import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringToolTest {
    private static Logger logger = LoggerFactory.getLogger(StringToolTest.class);

    @Test
    public void test() {
        logger.info("" + StringTool.isEmpty("  "));
        logger.info("" + StringTool.isBlank("  "));
    }

    @Test
    public void underlineToCamelCaseTest() {
        String text = "aaa_bbb";
        logger.info("text = " + text);
        logger.info("result = " + StringTool.underlineToCamelCase(text));
    }

}
