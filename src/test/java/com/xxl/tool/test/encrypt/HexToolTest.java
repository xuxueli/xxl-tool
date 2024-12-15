package com.xxl.tool.test.encrypt;

import com.xxl.tool.encrypt.HexTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexToolTest {
    private static Logger logger = LoggerFactory.getLogger(HexToolTest.class);

    @Test
    public void test(){
        String input = "xxl-tool";
        String output = HexTool.toHex(input);
        logger.info("input: {}, output: {}", input, output);

        String input2 = HexTool.fromHex(output);
        logger.info("calculate input2: {}", input2);

        Assertions.assertEquals(input, input2);
    }

}
