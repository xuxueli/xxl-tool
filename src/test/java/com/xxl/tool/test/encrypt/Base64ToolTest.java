package com.xxl.tool.test.encrypt;

import com.xxl.tool.encrypt.Base64Tool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64ToolTest {
    private static Logger logger = LoggerFactory.getLogger(Base64ToolTest.class);


    @Test
    public void test() {
        String str = "1234567890";
        logger.info(str);

        logger.info(Base64Tool.encodeStandard(str));
        logger.info(Base64Tool.decodeStandard(Base64Tool.encodeStandard(str)));

        logger.info(Base64Tool.encodeUrlSafe(str));
        logger.info(Base64Tool.decodeUrlSafe(Base64Tool.encodeUrlSafe(str)));
    }

}
