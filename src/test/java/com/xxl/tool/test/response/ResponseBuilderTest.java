package com.xxl.tool.test.response;

import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseBuilder;
import com.xxl.tool.response.ResponseCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(ResponseBuilderTest.class);

    @Test
    public void testResponseBuilder() {
        Response<String> response = new ResponseBuilder<String>()
                .code(ResponseCode.CODE_200.getCode())
                .msg("Sucess")
                .data("Hello World")
                .build();
        logger.info(response.toString());

        Assertions.assertEquals(response.getCode(),ResponseCode.CODE_200.getCode());
    }

}
