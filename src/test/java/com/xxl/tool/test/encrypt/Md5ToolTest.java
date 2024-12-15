package com.xxl.tool.test.encrypt;

import com.xxl.tool.encrypt.Md5Tool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5ToolTest {
    private static Logger logger = LoggerFactory.getLogger(Md5ToolTest.class);

    @Test
    public void test(){
        String input = "test";

        String output = Md5Tool.md5(input);
        logger.info("input:{}, md5:{}", input, output);
    }

}
