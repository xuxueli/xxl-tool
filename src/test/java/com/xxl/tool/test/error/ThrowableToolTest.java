package com.xxl.tool.test.error;

import com.xxl.tool.error.ThrowableTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrowableToolTest {
    private static final Logger logger = LoggerFactory.getLogger(ThrowableToolTest.class);

    @Test
    public void test() {
        logger.info("error : " + ThrowableTool.toString(new Exception("test")));
    }

}
