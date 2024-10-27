package com.xxl.tool.test.core;

import com.xxl.tool.core.AssertTool;
import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertToolTest {

    @Test
    public void isTrueTest() {
        AssertTool.isTrue(true, "not true");
    }

    @Test
    public void isNullTest() {
        AssertTool.notNull(new Object(), "not null");
    }

}
