package com.xxl.tool.test.id;

import com.xxl.tool.id.UUIDTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UUIDToolTest {
    private static Logger logger = LoggerFactory.getLogger(UUIDToolTest.class);

    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            logger.info("uuid=" + UUIDTool.getUUID());
        }

        for (int i = 0; i < 10; i++) {
            logger.info("uuid2=" + UUIDTool.getSimpleUUID());
        }
    }
}
