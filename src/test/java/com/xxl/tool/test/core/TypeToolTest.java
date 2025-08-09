package com.xxl.tool.test.core;

import com.xxl.tool.core.TypeTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeToolTest {
    private static Logger logger = LoggerFactory.getLogger(TypeToolTest.class);

    @Test
    public void test() {
        logger.info("{}", TypeTool.isAssignable(Number.class, Long.class));
        logger.info("{}", TypeTool.isAssignable(Long.class, Long.class));
    }

}
