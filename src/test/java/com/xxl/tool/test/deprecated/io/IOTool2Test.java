//package com.xxl.tool.test.io;
//
//import com.xxl.tool.io.IOTool2;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class IOTool2Test {
//    private static final Logger logger = LoggerFactory.getLogger(IOTool2Test.class);
//
//    @Test
//    public void testJson() {
//        try {
//            InputStream inputStream = new ByteArrayInputStream("input".getBytes());
//
//            byte[] result = IOTool2.copyToByteArray(inputStream);
//            logger.info(new String(result));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
