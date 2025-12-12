package com.xxl.tool.test.crypto;

import com.xxl.tool.crypto.Sha256Tool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA256ToolTest {
    private static Logger logger = LoggerFactory.getLogger(SHA256ToolTest.class);

    @Test
    public void test(){
        String input = "test";

        String output = Sha256Tool.sha256(input);
        String output2 = Sha256Tool.sha256(input, "123456");
        logger.info("input:{}, md5:{}", input, output);
        logger.info("input2:{}, md5:{}", input, output2);
    }

}
