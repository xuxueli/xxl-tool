package com.xxl.tool.test.http;

import com.xxl.tool.http.HttpTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpToolTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpToolTest.class);

    @Test
    public void test3() {
        String resp = HttpTool.postBody("http://www.baidu.com/", "hello world");
        logger.info(resp);
    }

    @Test
    public void test2() {
        String resp = HttpTool.postBody(
                "http://www.baidu.com/",
                "hello world",
                3000);
        logger.info(resp);
    }

    @Test
    public void test() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String resp = HttpTool.postBody(
                "http://www.baidu.com/",
                "hello world",
                headers,
                3000
                );
        logger.info(resp);
    }

    @Test
    public void test1() {
        String resp = HttpTool.get("http://www.baidu.com/");
        logger.info(resp);
    }

    @Test
    public void test4() {
        String resp = HttpTool.get("http://www.baidu.com/", 3000);
        logger.info(resp);
    }

    @Test
    public void test5() {
        String resp = HttpTool.get("http://www.baidu.com/", 3000, null);
        logger.info(resp);
    }

}
