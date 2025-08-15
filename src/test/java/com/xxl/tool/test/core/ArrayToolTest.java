package com.xxl.tool.test.core;

import com.xxl.tool.core.ArrayTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ArrayToolTest {
    private static Logger logger = LoggerFactory.getLogger(ArrayToolTest.class);


    @Test
    public void test() {
        logger.info("test: " + ArrayTool.isEmpty( null));
        logger.info("test: " + ArrayTool.isNotEmpty( null));
        logger.info("test: " + ArrayTool.contains( null, null));

        logger.info("test2: " + ArrayTool.isEmpty(Arrays.asList(1,2,3).toArray()));
        logger.info("test2: " + ArrayTool.isNotEmpty( Arrays.asList(1,2,3).toArray()));
        logger.info("test2: " + ArrayTool.contains( Arrays.asList(1,2,3).toArray(), 2));
    }

}
