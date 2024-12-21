package com.xxl.tool.test.net;

import com.xxl.tool.net.HttpTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpToolTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpToolTest.class);

    @Test
    public void test() {
        String resp = HttpTool.postBody("http://www.baidu.com/", "hello world", 3000);
        logger.info(resp);
    }

}
