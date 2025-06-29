package com.xxl.tool.test.id;

import com.xxl.tool.id.SnowflakeIdTool;
import org.junit.jupiter.api.Test;

public class SnowflakeToolTest {

    @Test
    public void test() {
        SnowflakeIdTool idGen = new SnowflakeIdTool(1);
        for (int i = 0; i < 10; i++) {
            System.out.println(idGen.nextId());
        }
    }
}
