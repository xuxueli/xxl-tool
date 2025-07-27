package com.xxl.tool.test.encrypt;

import com.xxl.tool.encrypt.SHA256Tool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA256ToolTest {
    private static Logger logger = LoggerFactory.getLogger(SHA256ToolTest.class);

    @Test
    public void test(){
        String input = "test";

        String output = SHA256Tool.sha256(input);
        String output2 = SHA256Tool.sha256(input, "123456");
        logger.info("input:{}, md5:{}", input, output);
        logger.info("input2:{}, md5:{}", input, output2);
    }

}
