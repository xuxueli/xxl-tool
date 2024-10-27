package com.xxl.tool.test.io;

import com.xxl.tool.io.StreamTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

public class StreamToolTest {
    private static final Logger logger = LoggerFactory.getLogger(StreamToolTest.class);

    @Test
    public void testJson() {
        try {
            InputStream inputStream = new ByteArrayInputStream("input".getBytes());

            byte[] result = StreamTool.copyToByteArray(inputStream);
            logger.info(new String(result));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
