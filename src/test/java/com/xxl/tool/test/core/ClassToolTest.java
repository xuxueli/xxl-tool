package com.xxl.tool.test.core;

import com.xxl.tool.core.ClassTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassToolTest {
    private static Logger logger = LoggerFactory.getLogger(ClassToolTest.class);

    @Test
    public void test() {
        logger.info("{}", ClassTool.isAssignable(Number.class, Integer.class));
        logger.info("{}", ClassTool.isAssignable(Integer.class, Integer.class));
        logger.info("{}", ClassTool.getMethod(Integer.class, "intValue"));
        logger.info("{}", ClassTool.getMethod(Integer.class, "valueOf", Integer.class));

    }
}
