//package com.xxl.tool.test.deprecated.cache2;
//
//import com.xxl.tool.deprecated.cache2.CacheTool;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CacheToolTest {
//    private static Logger logger = LoggerFactory.getLogger(CacheToolTest.class);
//
//    @Test
//    public void test() {
//        CacheTool.set("k1", "111", 10*1000);
//        logger.info("k1=" + CacheTool.get("k1"));
//        logger.info("k2=" + CacheTool.get("k2"));
//
//        CacheTool.remove("k1");
//        logger.info("k1=" + CacheTool.get("k1"));
//        logger.info("k2=" + CacheTool.get("k2"));
//    }
//
//}
