package com.xxl.tool.test.core;

import com.xxl.tool.core.PropTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PropToolTest {

    private static Logger logger = LoggerFactory.getLogger(PropToolTest.class);

    @Test
    public void test() {
        Properties prop = PropTool.loadProp("log4j.properties");
        System.out.println(PropTool.getString(prop, "log4j.rootLogger"));
    }

}
