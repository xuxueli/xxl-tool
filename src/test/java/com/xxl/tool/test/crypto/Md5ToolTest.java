package com.xxl.tool.test.crypto;

import com.xxl.tool.crypto.Md5Tool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5ToolTest {
    private static Logger logger = LoggerFactory.getLogger(Md5ToolTest.class);

    @Test
    public void test(){
        String input = "test";

        String output = Md5Tool.md5(input);
        String output2 = Md5Tool.md5(input, "123456");
        logger.info("input:{}, md5:{}", input, output);
        logger.info("input2:{}, md5:{}", input, output2);
    }

}
