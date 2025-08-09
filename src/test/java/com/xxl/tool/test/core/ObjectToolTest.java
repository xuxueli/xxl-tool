package com.xxl.tool.test.core;

import com.xxl.tool.core.ObjectTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ObjectToolTest {
    private static Logger logger = LoggerFactory.getLogger(ObjectToolTest.class);

    @Test
    public void test() {
        logger.info("result = " + ObjectTool.toString(this));
        logger.info("result = " + ObjectTool.getIdentityHexString( this));
        logger.info("result = " + !ObjectTool.equal(1, 2));
        logger.info("result = " + ObjectTool.equal(this, this));
        logger.info("result = " + !ObjectTool.equal(Long.valueOf(2), 2));
        logger.info("result = " + ObjectTool.isArray(new String[]{"1","2"}));
        logger.info("result = " + ObjectTool.isEmpty(new String[]{}));
    }

}
